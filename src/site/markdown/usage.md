Usage
=====

1. Install a brand new Eclipse into your new location, for example ``eclipse-new``
2. Copy into the folder ``eclipse-new/dropins``  your old Eclipse installation, 
   e.g. copy ``ecipse-old/plugins`` and ``eclipse-old/features`` into ``eclipse-new/dropins/eclipse``,
   so you will have the following file structure:
  
    ```
    |-- eclipse-new
    |   |-- features (new)
    |   |-- plugins (new)
    |   |-- dropins
    |       |-- eclipse
    |           |-- features (old)
    |           |-- plugins (old)
    ``` 

3. Build ``eclipse-plugin-cleaner`` or download from https://github.com/azachar/eclipse-plugin-cleaner/releases compiled jar with Java 7.
4. Copy ``plugin-cleaner-x.x.x-jar-with-dependencies.jar`` into the folder ``eclipse-new`` and execute this command
 
    ```
    java -jar plugin-cleaner-x.x.x.-jar-with-dependencies.jar
    ```
 
 * You can specify the option ``-test`` to run the dry mode that simulates changes without any modifications!
 * For more options please use the option ``-help``
 * **Duplicates are moved into the folder ``<source-folder>\duplicates_<timestamp>``**, e.g. in ``eclipse-new\duplicates_<timestamp>``
5. Run your new Eclipse
6. Delete ``eclipse-new\duplicates_<timestamp>`` if everything works.
7. Now you have the brand new installation of Eclipse with your custom plugins!


Command line parameters
=======================
 usage: ``java -jar plugin-cleaner-XXX-jar-with-dependencies.jar   [-d <destination>] [-h] [-m <mode>] [-s <source>] [-t]``
 
 * ``-s,--source <source>`` The path to an Eclipse installation to clean up. The default is the current folder.
 * ``-d,--destination <destination>`` The path to a folder where duplicated bundles will be moved. The default is the absolute path to ``<source>/duplicates-<timestamp>``.

 * **``-m,--mode <mode>``** Allows to specify a duplication detection mode as follows: 
    * **``dropinsOnly``** (default) Duplicates could be only artifacts located in the ``dropins`` folder.
    * ``prefereDropins`` If there are two bundles with the same version then the bundle that is in the ``dropins`` folder is considered to be duplicated. If both of them are from a ``non-dropins`` folder than first come is kept the second one is marked as a duplicate.
    * ``unlimited`` Resolves duplicates regardless their location.

 * ``-t,--test`` Enables a dry run mode, e.g. no action will be taken.
 * ``-h,--help`` Shows help.


Known Limitations
==================

The bundle duplications resolving is based on a fast version duplication analysis.

In the Eclipse (and in any other OSGI enabled) application you can have a bundle that depends on multiple versions of a different bundle.
Typically Eclipse doesn't contains multiple versions of the same bundle, but if your installation contains such a setup than additional actions are required.

After you do the clean up by this tool go to Eclipse, choose ``Window -> Show View -> Error Log`` 
and check if there is any missing required bundle since you have done the clean up. If so simply move missing required features and bundles from the duplicated folder back to your ``eclipse-new/features`` and/or ``eclipse-new/plugins`` or ``eclipse-new/dropins`` folders.