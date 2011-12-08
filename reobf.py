#!/usr/bin/env python

import csv, re, os, fnmatch

db = {}

# Regex pattern matching quoted "field_NNN_X" and "func_NNN_X" strings
pattern = re.compile('"((field|func)_[0-9]+_[a-zA-Z]+)"')

# Add each row in CSV file to "index" using first column as dict key
def index(name):
    reader = csv.reader(open(name, "rb"))
    for row in reader:
        db[row[0]] = row

# Regex replacement function. Returns obfuscated name for Searge name
def replace(matchobj):
    row = db[matchobj.group(1)]
    return '"' + row[2] + '"'

# Index all the field and method data provided by Minecraft Coder Pack
index("conf/fields.csv")
index("conf/methods.csv")

# Read every "*.java" file in "reobf/src" subtree, replace all quoted strings
# containing Searge style field/method names in each source file, and write
# the post processed file to the same relative directory in "src" subtree.
for (dirpath, dirs, files) in os.walk("reobf" + os.sep + "src"):
    for name in fnmatch.filter(files, "*.java"):
        path = os.path.join(dirpath, name);

        # Read file in and perform replacements
        with file(path, "rb") as f:
            data = f.read()
        data = pattern.sub(replace, data)

        # Remove the "reobf" directoryprefix from path and write new file
        path = os.path.join(*path.split(os.sep)[1:])        
        with file(path, "wb") as f:
            f.write(data)
