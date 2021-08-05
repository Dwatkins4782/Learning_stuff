package com.ercot.puppetfiles.v1.controller;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ercot.util.GitClient;
import com.ercot.util.GitFileData;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@RestController
@OpenAPIDefinition(info = @Info(title = "Puppetfiles", description = "REST API for maintaining the lifecycle of puppetfiles", version = "v1.0.3", license = @License(name = "ERCOT", url = "http://www.ercot.com")))
@PropertySource(value = {"classpath:application.properties", "classpath:puppetfiles.properties", "file:/etc/opt/ercot/puppetfiles/configuration/application.properties", "file:/etc/opt/ercot/puppetfiles/secrets/puppetfiles.properties"}, ignoreResourceNotFound = true)
public class PuppetfilesController {

    private static final String PUPPET_MD5_HEADER = "Content-MD5";

    private static final String PUPPET_CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    private static final String CONTENT_DISPOSITION = "inline; filename=\"%1$s\"; filename*=UTF-8\'\'%1$s";

    @Value( "${com.ercot.puppetfiles.repoUrl:https://stash.ercot.com/scm/pup/jira_yaml_file.git}" )
    private String repoUrl;

    @Value( "${com.ercot.puppetfiles.username:puppetfiles}" )
    private String username;

    @Value( "${com.ercot.puppetfiles.password:!V^OfvM@*Ke%}" )
    private String password;

    private Path repositoryPath;

    private static final Logger logger = LoggerFactory.getLogger(PuppetfilesController.class);

    private static final Histogram PUPPETFILES_LATENCY_HISTOGRAM = Histogram.build().name("PuppetfilesLatencyHistogram").help("Latency Histogram for Puppetfiles Calls").register();

    private static final Histogram PUPPETFILES_CREATEORUPDATE_LATENCY_HISTOGRAM = Histogram.build().name("PuppetfilesCreateLatencyHistogram").help("Latency Histogram for PuppetfilesController.createPuppetfile Calls").register();
    private static final Counter CREATEORUPDATE_COUNTER = Counter.build().name("PuppetfilesCreateCounter").help("Counter for createPuppetfile Calls").register();

    private static final Histogram PUPPETFILES_READ_LATENCY_HISTOGRAM = Histogram.build().name("PuppetfilesReadLatencyHistogram").help("Latency Histogram for PuppetfilesController.readPuppetfile Calls").register();
    private static final Counter READ_COUNTER = Counter.build().name("PuppetfilesReadCounter").help("Counter for readPuppetfile Calls").register();

    private static final Histogram PUPPETFILES_DELETE_LATENCY_HISTOGRAM = Histogram.build().name("PuppetfilesDeleteLatencyHistogram").help("Latency Histogram for PuppetfilesController.deletePuppetfile Calls").register();
    private static final Counter DELETE_COUNTER = Counter.build().name("PuppetfilesDeleteCounter").help("Counter for deletePuppetfile Calls").register();

    @EventListener(ApplicationReadyEvent.class)
    public void initializeLocalRepoCopy() {
        try {
            if(logger.isInfoEnabled()) {
                logger.info("Initializing local jira_yaml_file clone.");
            }
            FS.DETECTED.setUserHome(new File("/opt"));
            repositoryPath = GitClient.clone(repoUrl, username, password);
            if(logger.isInfoEnabled()) {
                logger.info("Clone initialization complete.");
            }
        } catch (GitAPIException gae) {
            logger.error(gae.getMessage(), gae);
        }
    }

    @Operation(summary = "Create or update a Puppetfile.")
    @PostMapping(value = {"/puppetfiles/v1/create/{host}/{type}", "/puppetfiles/v1/update/{host}/{type}"}, consumes = {MediaType.TEXT_PLAIN_VALUE})
    void createPuppetfile(@Parameter(required = true, description = "Host name for the managed device.") @PathVariable final String host,
                          @Parameter(required = true, description = "Puppetfile type. One of either \"jira.yaml\" or \"ldap.yaml\".") @PathVariable final String type, @RequestBody String fileContent, final Authentication authentication) throws GitAPIException {
        final String file = host.toUpperCase(Locale.getDefault()) + "/" + type;
        if(logger.isInfoEnabled()) {
            logger.info("Incoming request for /puppetfiles/v1/createOrUpdate/" + file);
        }

        try(Histogram.Timer timer = PUPPETFILES_LATENCY_HISTOGRAM.startTimer();
            Histogram.Timer createTimer = PUPPETFILES_CREATEORUPDATE_LATENCY_HISTOGRAM.startTimer()) {
            CREATEORUPDATE_COUNTER.inc();
            GitClient.pull(repositoryPath, username, password);
            GitClient.writeFileData(repositoryPath, fileContent, file);
            GitClient.commitAndPush(repositoryPath, "API CreateOrUpdate " + file, false, username, password, authentication.getName());
            if(logger.isInfoEnabled()) {
                logger.info("Finishing request for /puppetfiles/v1/createOrUpdate/" + file + " took " + timer.observeDuration());
            }
            PUPPETFILES_LATENCY_HISTOGRAM.observe(timer.observeDuration());
            PUPPETFILES_CREATEORUPDATE_LATENCY_HISTOGRAM.observe(createTimer.observeDuration());
        }
    }

    @Operation(summary = "Retrieve an existing Puppetfile.")
    @GetMapping(value = "/puppetfiles/v1/read/{host}/{type}", produces = {MediaType.TEXT_PLAIN_VALUE}, consumes = {MediaType.ALL_VALUE} )
    String readPuppetfile(@Parameter(required = true, description = "Host name for the managed device.") @PathVariable final String host,
                          @Parameter(required = true, description = "Puppetfile type. One of either \"jira.yaml\" or \"ldap.yaml\".") @PathVariable final String type,
                          final HttpServletResponse response, final Authentication authentication) throws GitAPIException {
        final String file = host.toUpperCase(Locale.getDefault()) + "/" + type;
        if(logger.isInfoEnabled()) {
            logger.info("Incoming request for /puppetfiles/v1/read/" + file);
        }
        String retval = null;
        try(Histogram.Timer timer = PUPPETFILES_LATENCY_HISTOGRAM.startTimer();
            Histogram.Timer readTimer = PUPPETFILES_READ_LATENCY_HISTOGRAM.startTimer()) {
            READ_COUNTER.inc();
            GitClient.pull(repositoryPath, username, password);
            GitFileData gfd = GitClient.findFileData(repositoryPath, file);
            retval = gfd.getContent();
            response.addHeader(PUPPET_MD5_HEADER, gfd.getChecksum());
            response.addHeader(PUPPET_CONTENT_DISPOSITION_HEADER, String.format(CONTENT_DISPOSITION, type));
            if(logger.isInfoEnabled()) {
                logger.info("Finishing request for /puppetfiles/v1/read/" + file + " took " + timer.observeDuration() + " file checksum: " + gfd.getChecksum());
            }
            PUPPETFILES_LATENCY_HISTOGRAM.observe(timer.observeDuration());
            PUPPETFILES_READ_LATENCY_HISTOGRAM.observe(readTimer.observeDuration());
        } 

        return retval;
    }

    @Operation(summary = "Delete an existing Puppetfile.")
    @DeleteMapping(value = "/puppetfiles/v1/delete/{host}/{type}")
    void deletePuppetfile(@Parameter(required = true, description = "Host name for the managed device.") @PathVariable final String host,
                          @Parameter(required = true, description = "Puppetfile type. One of either \"jira.yaml\" or \"ldap.yaml\".") @PathVariable final String type, final Authentication authentication) throws GitAPIException {
        deleteFile(host, type, authentication);
    }

    @Operation(summary = "Delete an existing host and all of its Puppetfiles.")
    @DeleteMapping(value = "/puppetfiles/v1/delete/{host}")
    void deletePuppetfile(@Parameter(required = true, description = "Host name for the managed device.") @PathVariable final String host, final Authentication authentication) throws GitAPIException {
        deleteFile(host, null, authentication);
    }

    private void deleteFile(final String host, final String type, final Authentication authentication) throws GitAPIException {
        final String file;
        if(type != null) {
            file = host.toUpperCase(Locale.getDefault()) + "/" + type;
        } else {
            file = host.toUpperCase(Locale.getDefault());
        }

        if(logger.isInfoEnabled()) {
            logger.info("Incoming request for /puppetfiles/v1/delete/" + file);
        }

        try(Histogram.Timer timer = PUPPETFILES_LATENCY_HISTOGRAM.startTimer();
            Histogram.Timer createTimer = PUPPETFILES_DELETE_LATENCY_HISTOGRAM.startTimer()) {
            DELETE_COUNTER.inc();
            GitClient.pull(repositoryPath, username, password);
            GitClient.deleteFile(repositoryPath, file);
            GitClient.commitAndPush(repositoryPath, "API Delete " + file, true, username, password, authentication.getName());
            if(logger.isInfoEnabled()) {
                logger.info("Finishing request for /puppetfiles/v1/delete/" + file + " took " + timer.observeDuration());
            }
            PUPPETFILES_LATENCY_HISTOGRAM.observe(timer.observeDuration());
            PUPPETFILES_DELETE_LATENCY_HISTOGRAM.observe(createTimer.observeDuration());
        }
    }
}