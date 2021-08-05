package com.ercot.util;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

public class GitClient {

    private static final Histogram PUPPETFILES_CLONE_LATENCY_HISTOGRAM = Histogram.build().name("PuppetfilesCloneLatencyHistogram").help("Latency Histogram for GitClient.clone Calls").register();
    private static final Counter CLONE_COUNTER = Counter.build().name("PuppetfilesCloneCounter").help("Counter for GitClient.clone Calls").register();

    private static final Histogram PUPPETFILES_PULL_LATENCY_HISTOGRAM = Histogram.build().name("PuppetfilesPullLatencyHistogram").help("Latency Histogram for GitClient.pull Calls").register();
    private static final Counter PULL_COUNTER = Counter.build().name("PuppetfilesPullCounter").help("Counter for GitClient.pull Calls").register();

    private static final Histogram PUPPETFILES_COMMITPUSH_LATENCY_HISTOGRAM = Histogram.build().name("PuppetfilesCommitPushLatencyHistogram").help("Latency Histogram for GitClient.commitAndPush Calls").register();
    private static final Counter COMMITPUSH_COUNTER = Counter.build().name("PuppetfilesCommitPushCounter").help("Counter for GitClient.commitAndPush Calls").register();

    private static final Histogram PUPPETFILES_SEARCH_LATENCY_HISTOGRAM = Histogram.build().name("PuppetfilesSearchLatencyHistogram").help("Latency Histogram for GitClient.findFile Calls").register();
    private static final Counter SEARCH_COUNTER = Counter.build().name("PuppetfilesSearchCounter").help("Counter for GitClient.findFile Calls").register();

    private static final Logger logger = LoggerFactory.getLogger(GitClient.class);

    private GitClient() {}

    public static Path clone(final String repoUrl, final String username, final String password) throws GitAPIException {
        Path retval = null;
        if (repoUrl != null) {
            try (Histogram.Timer timer = PUPPETFILES_CLONE_LATENCY_HISTOGRAM.startTimer()) {
                CLONE_COUNTER.inc();
                retval = Files.createTempDirectory(null);
                Git.cloneRepository().setURI(repoUrl)
                   .setDirectory(retval.toFile()).setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).call();
                PUPPETFILES_CLONE_LATENCY_HISTOGRAM.observe(timer.observeDuration());
                if(logger.isInfoEnabled()) {
                    logger.info("Incoming request for GitClient.clone took: " + timer.observeDuration());
                }
            } catch (IOException exec) {
                logger.error(exec.getMessage(), exec);
            }
        }

        return retval;
    }

    public static void pull(final Path repo, final String username, final String password) throws GitAPIException {
        if (repo != null) {
            try (Histogram.Timer timer = PUPPETFILES_PULL_LATENCY_HISTOGRAM.startTimer()) {
                PULL_COUNTER.inc();
                Git.open(repo.toFile()).pull().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).call();
                PUPPETFILES_PULL_LATENCY_HISTOGRAM.observe(timer.observeDuration());
                if(logger.isInfoEnabled()) {
                    logger.info("Incoming request for GitClient.pull took: " + timer.observeDuration());
                }
            } catch (IOException exec) {
                logger.error(exec.getMessage(), exec);
            }
        }
    }

    public static void commitAndPush(final Path repo, final String message, boolean remove, final String username, final String password, String authenticatedUser) throws GitAPIException {
        if(repo != null) {
            try (Histogram.Timer timer = PUPPETFILES_COMMITPUSH_LATENCY_HISTOGRAM.startTimer()) {
                COMMITPUSH_COUNTER.inc();
                try(Git clone = Git.open(repo.toFile())) {
                    clone.add().setUpdate(remove).addFilepattern(".").call();
    
                    if(logger.isInfoEnabled()) {
                        logger.info("Git Added: " + clone.status().call().getAdded());
                        logger.info("Git Changed: " + clone.status().call().getChanged());
                        logger.info("Git Removed: " + clone.status().call().getRemoved());
                    }
                        
                    clone.commit().setAuthor(new PersonIdent(authenticatedUser, authenticatedUser + "@ercot.com")).setCommitter(new PersonIdent(username, username + "@ercot.com")).setMessage(message).call();
                    clone.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).call();
                    PUPPETFILES_COMMITPUSH_LATENCY_HISTOGRAM.observe(timer.observeDuration());
                    
                    if(logger.isInfoEnabled()) {
                        logger.info("Incoming request for GitClient.commitAndPush took: " + timer.observeDuration());
                    }
                }
            } catch (IOException exec) {
                logger.error(exec.getMessage(), exec);
            }
        }
    }

    public static final GitFileData findFileData(final Path repo, final String file) {
        GitFileData retval = null;
        if (file != null) {
            try (Histogram.Timer timer = PUPPETFILES_SEARCH_LATENCY_HISTOGRAM.startTimer();
                    Git repoRef = Git.open(repo.toFile())) {
                SEARCH_COUNTER.inc();
                final Repository repository = repoRef.getRepository();
                final ObjectId lastCommitId = repository.resolve(Constants.HEAD);

                try (RevWalk revWalk = new RevWalk(repository)) {
                    final RevCommit commit = revWalk.parseCommit(lastCommitId);
                    final RevTree tree = commit.getTree();

                    try (TreeWalk treeWalk = new TreeWalk(repository)) {
                        treeWalk.addTree(tree);
                        treeWalk.setRecursive(true);
                        treeWalk.setFilter(PathFilter.create(file));
                        if (!treeWalk.next()) {
                            throw new IllegalStateException("Did not find expected file '" + file + "'");
                        }

                        final ObjectId objectId = treeWalk.getObjectId(0);
                        final ObjectLoader loader = repository.open(objectId);

                        retval = new GitFileData(new String(loader.getBytes(), StandardCharsets.UTF_8), computeContentMD5Header(new ByteArrayInputStream(loader.getBytes())));
                    }
                }
                PUPPETFILES_SEARCH_LATENCY_HISTOGRAM.observe(timer.observeDuration());
                if(logger.isInfoEnabled()) {
                    logger.info("Incoming request for GitClient.findFile took: " + timer.observeDuration());
                }
            } catch (IOException exec) {
                logger.error(exec.getMessage(), exec);
            }
        }

        return retval;
    }

    public static String computeContentMD5Header(final InputStream inputStream) {
        String retval = null;

        try (DigestInputStream stream = new DigestInputStream(inputStream,MessageDigest.getInstance("MD5"))) {
            byte[] buffer = new byte[8192];
            int bytesRead = 0;

            do {
                bytesRead = stream.read(buffer);
            } while (bytesRead > 0);

            retval = new String(Base64.encodeBase64(stream.getMessageDigest().digest()), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | IOException exec) {
            logger.error(exec.getMessage(), exec);
        }

        return retval;
    }

    public static void writeFileData(final Path repositoryPath, final String fileContent, final String filePath) {
        if(repositoryPath != null && fileContent != null && !fileContent.isEmpty() && filePath != null) {
            final File child = new File(repositoryPath.toString() + "/" + filePath);
            final File parent = new File(repositoryPath.toString() + "/" + filePath.split("/")[0]);
            if(child != null && parent != null) {
                if(!parent.exists()) {
                    parent.mkdirs();
                }
                try {
                    if(child.createNewFile()) {
                        logger.info("GitClient.writeFileData creating new file " + filePath);
                    } else {
                        logger.info("GitClient.writeFileData replacing existing file contents " + filePath);
                    }
                } catch (IOException exec) {
                    logger.error(exec.getMessage(), exec);
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(child))) {
                    writer.write(fileContent);
                    writer.flush();
                } catch (IOException exec) {
                    logger.error(exec.getMessage(), exec);
                }
            }
        }
    }

    private static void deleteDirectory(final File parent) {
        if (parent != null) {
            for (File child : parent.listFiles()) {
                if(child.isFile()) {
                    child.delete();
                } else {
                    deleteDirectory(child);
                    child.delete();
                }
            }
            parent.delete();
        }
    }

    public static void deleteFile(final Path repositoryPath, final String filePath) {
        if(repositoryPath != null && filePath != null) {
            final File target = new File(repositoryPath + "/" + filePath);
            if(target != null && target.exists()) {
                if(target.isDirectory()) {
                    deleteDirectory(target);
                } else {
                    target.delete();
                }
            }
        }
    }
}