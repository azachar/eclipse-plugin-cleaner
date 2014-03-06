Eclipse Plugin Cleaner [![Build Status](https://travis-ci.org/azachar/eclipse-plugin-cleaner.png)](https://travis-ci.org/azachar/eclipse-plugin-cleaner)
======================

Helps to clean up your eclipse installation from duplicated plugins and features. The duplicated plugins and features are move into separate folder.
So you can decide later to delete them if no needed.

To find duplicated bundles this program checks manifests or filenames. The newest version is preserved.
By default program try to removes duplicates from the dropins (sub)folder.

Usage
=============

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

3. Build ``eclipse-plugin-cleaner`` or download from https://github.com/azachar/eclipse-plugin-cleaner/releases compiled jar. 
4. Copy ``plugin-cleaner-0.x.x.-jar-with-dependencies.jar`` into the folder ``eclipse-new`` and execute this command
 ```
  java -jar plugin-cleaner-0.x.x.-jar-with-dependencies.jar
 ```
 * You can specify the option ``-test`` to run it in the dry run mode that simulates changes!
 * For more options please run only with the option ``-help``
 * **Duplicates are moved into the folder ``eclipse-new\duplicates_<timestamp>``**
5. Run your new Eclipse
6. Delete ``eclipse-new\duplicates_<timestamp>`` if everything works.
7. Now you have the brand new installation of Eclipse with your custom plugins!
