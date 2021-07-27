# Cognos
Roles for Cognos servers. There are three different designations: gateway, report, content. There is also a "common" role for tasks that apply to all machines in the stack.

The "report" and "content" designations are identical but for their use of different response files when executing the installer. Servers designated as "content" are actually running both the reporting and content services, but you must use the "content" role.

## Dependencies
Most of the roles pull artifacts from http://puppetweb.ercot.com/. See details in each defaults/main.yml for specific URLs.
