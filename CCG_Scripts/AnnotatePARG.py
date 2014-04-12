# When run in the base directory of CCGBank, this script creates a new
# directory of PARG files called ANNO which have been annotated with the
# surface forms:  <s> #parses word1|tag1|cat1 word2|tag2|cat2 ....
# Author:  Yonatan Bisk (bisk1@illinois.edu)

import os,sys

os.mkdir("ANNO")
for root,dirs,files in os.walk("PARG/"):
  for d in dirs:
    os.mkdir("ANNO/" + d)
for root,dirs,files in os.walk("PARG/"):
  for name in files:
    parg_f = os.path.join(root, name)
    anno_f = parg_f.replace("PARG","ANNO")
    auto_f = parg_f.replace("parg","auto").replace("PARG","AUTO")
    print parg_f, auto_f
    sents = []
    AUTO = open(auto_f,'r')
    for line in AUTO:
      if line.split("=")[0] != "ID":
        line = line.split("(<L")[1:]
        S = ""
        for chunk in line:
          chunk = chunk.split()
          S += chunk[3] + "|" + chunk[1] + "|" + chunk[0] + " "
        sents.append(S)
    AUTO.close()
    PARG = open(parg_f, 'r')
    ANNO = open(anno_f, 'w')
    for line in PARG:
      if line.split()[0] == "<s>" and \
          (line.split()[1] != "0" or \
          (len(sents) > 0 and len(sents[0].split()) == 1)):
        ANNO.write("<s> " + line.split()[1] + "\t" + sents.pop(0) + "\n")
      else:
        ANNO.write(line)
    PARG.close()
    ANNO.close()
