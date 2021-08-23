import os
import sys


TXZ_GENERAL_PRJ_ROOT = os.path.abspath(os.path.join(os.getcwd(), "../txz_general"))
TXZ_GENERAL_PRJ_ROOT = TXZ_GENERAL_PRJ_ROOT.replace("\\", '/')

print TXZ_GENERAL_PRJ_ROOT

content = open('.project.tmpl').read()
content = content.replace('$(TXZ_GENERAL_PRJ_ROOT)', TXZ_GENERAL_PRJ_ROOT)

open('.project', 'w').write(content)
