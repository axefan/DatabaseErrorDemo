This plugin demonstrates a problem that I've been having with the EbeanServer 
object provided by JavaPlugin.getDatabase().

EbeanServer.save(Collection) starts failing when a list contains around 7000 
objects.

Load this plugin and try sizes of 1 to 5000.  All pass checks.

Try a size of 10000 and several objects will not get written even though no 
exception is thrown and the return value indicates that all objects were 
written.

By default the plugin will save objects in batch mode using a List.
To force the test to save individual objects, toggle the batch setting...

  \ded batch
  \ded b
  
By default the plugin will use journaling.
To disable journaling , toggle the journal setting...

  \ded journal
  \ded j

By default, the plugin will delete any records that it creates.
To disable this, toggle the cleanup setting...

  \ded cleanup
  \ded c
  