# When run with a reference to your evaluation section of CCGBank and file to
# evaluate, annotates your file with:
# <s> #parses word1|tag1|cat1 word2|tag2|cat2 ....
# Author:  Yonatan Bisk (bisk1@illinois.edu)
import os,sys

if len(sys.argv) != 3:
  print "python AnnotateYourFile.py CCGbank/AUTO/23 MyFile.parg"
  sys.exit()

Gold_Dir   = sys.argv[1]
Input_File = sys.argv[2]

print Gold_Dir, Input_File
sents = []
for root,dirs,files in os.walk(Gold_Dir):
  for name in files:
    f = os.path.join(root, name)
    AUTO = open(f,'r')
    for line in AUTO:
      if line.split("=")[0] != "ID":
        line = line.split("(<L")[1:]
        S = ""
        for chunk in line:
          chunk = chunk.split()
          S += chunk[3] + "|" + chunk[1] + "|" + chunk[0] + " "
        sents.append(S)
    AUTO.close()
print "Annotating ", len(sents), " sentences"

PARG = open(Input_File,'r')
ANNO = open(Input_File + ".anno",'w')
count = 0
for line in PARG:
  try:
    if line.split()[0] == "<s>" and line.split()[1] != "0":
      ANNO.write("<s> " + line.split()[1] + "\t" + sents.pop(0) + "\n")
    else:
      ANNO.write(line)
  except:
    print "The number of sentences do not match"
    sys.exit()
PARG.close()
ANNO.close()
