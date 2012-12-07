#!/usr/bin/env python
#
# -*- coding: utf-8 -*-
#
# Desc: The script will recursively process writable files 
#       in @root directory. It will do following two things:
#       1. strip tailing spaces in specific files
#       2. convert tab to 4 spaces.
#
# Author: Chen Ming
#
#

import os
import re
from stat import *


def main():
    # The root directory to be proccessed.
    root = '.'

    # Expand tab to spaces ?
    expandtabs = True

    # Tab size to spaces
    tabsize = 4

    # To be processed file's pattern.
    # Current process all *.cpp *.c *.h and Jamefile
    pattern = re.compile("\.cpp$|\.c$|\.h$|^Jamfile$|\.js$|\.xml$|\.java$", re.IGNORECASE);

    os.chdir(root)

    for dirpath, dirs, files in os.walk(root):
        for name in files:
            path = os.path.join(dirpath, name)
            #print "path: %s, name: %s" % (path, name)
            if pattern.search(name):
                mode = os.stat(path)[ST_MODE]
                if not S_ISREG(mode):
                    # non-regular file
                    continue

                if not mode & S_IRUSR:
                    # no read permission
                    continue

                if not mode & S_IWUSR:
                    # no write permission - read only
                    continue

                strip_file(path, expandtabs, tabsize)


def strip_file(path, expandtabs=True, tabsize=4):
    """
        strip file @path's taling white spaces,
        if @expandtabs is True, then expand tab
        to @tabsize spaces
    """

    print " Processing %s..." % path
    f = open(path, 'r')
    newcontent = ''
    for line in f:
        if expandtabs:
            line = line.expandtabs(tabsize)
        line = line.rstrip()
        newcontent += line
        newcontent += '\n'
    f.close()

    f = open(path, 'w')
    f.write(newcontent)
    f.close()

if __name__ == '__main__':
    main()

