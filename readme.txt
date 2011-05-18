This plugin demonstrates a problem that I've been having with the EbeanServer 
object provided by JavaPlugin.getDatabase().

Not all records are being saved!

Load this plugin and try sizes of 1 to 5000.  I have not seen any problems.

Try a size of 10000 and several objects will not get written even though no 
exception is thrown and the return value indicates that all objects were 
written.

By default the plugin will use transactions.
To disable transactions and use a List instead, use the 'tx' command...

  \ded tx
  
Originally, I thought that this problem was related to the 
EbeanServer.save(Collection) method, but this demo proves that it is not.