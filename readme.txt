This plugin demonstrates a problem that I've been having.

EbeanServer.save(Collection) starts failing when a list contains around 7000 objects.

Load this plugin and try sizes of 1 to 5000.  All pass checks.

Try a size of 10000 and several objects will not get written even though no exception is 
thrown and the return value indicates that all objects were written.