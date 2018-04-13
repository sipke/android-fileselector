Android Editable File Selector Fragment
======================================

This is an android list view fragment which is file selector.
The user specifies the directory to list files in (no sub directories)
and the list view shows the files.
The list has two modes.
1. select mode where when a user selects a file an event is raised 
   to a listener.
2. edit mode, where multiple files can be deleted or single files renamed.

Initial version does not have auto update of the list, so caller has to
inform when they wish to refresh the list.
