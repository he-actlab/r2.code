#!/home/jspark/projects/expax/python-2.7/python

import os, sys

os.system("sed -i 's/\/usr\/bin\/python/\/home\/jspark\/projects\/expax\/python-2.7\/python/g' *.py")
os.system("sed -i 's/\/usr\/bin\/python/\/home\/jspark\/projects\/expax\/python-2.7\/python/g' ../apps/*.py")
os.system("sed -i 's/\/usr\/bin\/python/\/home\/jspark\/projects\/expax\/python-2.7\/python/g' ../apps/*.py.template")
os.system("sed -i 's/\/usr\/local\/bin\/python2.7/\/home\/jspark\/projects\/expax\/python-2.7\/python/g' ../apps/*.py")