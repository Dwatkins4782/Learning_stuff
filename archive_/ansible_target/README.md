# ansible_target #

###Summary
This role is meant to prepare ANY node to be a target for ansible. The playbook is meant to be run as your normal user (since ansible doesn't exist yet.)

###Tasks
- Install python-simplejson (required for systems with Python < 2.6)
- Create the ansible user and group.
- Push admin keys into ansible's authorized_keys
- Remove ansible user's password expiration.
- Add ansible to /etc/sudoers.d/Super_Admins

###Usage
First please open up *create_ansible_user.yml* to make sure that the **hosts** and **remote_user** settings are what you want.

```$ ansible-playbook ansible_target.yml```

Also keep in mind that you can use tags to limit which tasks you run on hosts. This whole playbook is a little slow, so it's probably a good idea to limit it if you can.

###Notes
Room for optimization with installing python-simplejson...presently the task always does it, but that's not actually required on RHEL 6.

The change to the sudoers.d file is **destructive**! It does a hard replacement of the relevant line. Puppet's template now does have ansible in there so this bit is technically only needed in Production while it is still in noop mode.
