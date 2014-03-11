Usage
=====

1. Install a brand new Eclipse to your new location, for example ``eclipse-new``
2. Copy your old Eclipse installation into the folder ``eclipse-new/dropins``, 
   e.g. Copy ``eclipse-old/plugins`` and ``eclipse-old/features`` into ``eclipse-new/dropins/eclipse``,
   to have the following structure:
  
    ```
    |-- eclipse-new
    |   |-- features (new)
    |   |-- plugins (new)
    |   |-- dropins
    |       |-- eclipse
    |           |-- features (old)
    |           |-- plugins (old)
    ``` 

3. Download the latest release of ``eclipse-plugin-cleaner`` from https://github.com/azachar/eclipse-plugin-cleaner/releases (compiled with Java 7) or compile it from the source code.
4. Copy ``plugin-cleaner-x.x.x-jar-with-dependencies.jar`` into the folder ``eclipse-new`` and execute the following command
 
    ```
    java -jar plugin-cleaner-x.x.x.-jar-with-dependencies.jar
    ```
 
    * You can specify the option ``-test`` to run the dry mode that simulates changes without any modifications. 
    * **Duplicates are moved into the folder ``<source-folder>\duplicates_<timestamp>``**, e.g. in ``eclipse-new\duplicates_<timestamp>``.

5. Run the new Eclipse.
6. Delete ``eclipse-new\duplicates_<timestamp>`` if everything works.
7. Now you have a brand new installation of Eclipse with your custom plugins!


Command Line Parameters
=======================
 usage: ``java -jar plugin-cleaner-XXX-jar-with-dependencies.jar   [-d <destination>] [-h] [-m <mode>] [-s <source>] [-t]``
 
 * ``-s,--source <source>`` Path to Eclipse installation. The default is the current folder.
 * ``-d,--destination <destination>`` Path to folder where duplicated bundles will be moved. The default is the absolute path to ``<source>/duplicates-<timestamp>``.

 * **``-m,--mode <mode>``** To specify a duplication detection mode as follows: 
    * **``dropinsOnly``** (default) Duplicates can only be artifacts located in the ``dropins`` folder.
    * ``prefereDropins`` If there are two bundles with the same version, then the bundle that is in the ``dropins`` folder is considered to be duplicated. If both of them are from a ``non-dropins`` folder, than the first one is kept and the second one is marked as a duplicate.
    * ``unlimited`` Resolves duplicates regardless their location.

 * ``-t,--test`` Enables a dry run mode, e.g. no action will be taken.
 * ``-h,--help`` Shows help.


Known Limitations
==================

The bundle duplication resolution is based on a quick duplication analysis.

In the Eclipse application you can have a bundle that depends on multiple versions of a different bundle.
Typically Eclipse doesn't contain multiple versions of the same bundle, but if your installation set up this way, then additional actions are required.

After you do the clean up with this tool, go to Eclipse, choose ``Window -> Show View -> Error Log`` 
and check if any required bundles are missing as you have completed the clean up. If so, simply move the missing required features and bundles from the duplicated folder back to your ``eclipse-new/features`` and/or ``eclipse-new/plugins`` or ``eclipse-new/dropins`` folders.