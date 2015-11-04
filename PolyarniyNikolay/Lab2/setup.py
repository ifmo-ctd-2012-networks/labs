#!python3.4

__author__ = "Polyarnyi Nickolay"

from setuptools import setup, find_packages

setup(name='task2',
      version='0.1',
      packages=find_packages('src'),
      package_dir={'': 'src'},
      install_requires=[
          'netifaces>=0.10.4',
          'PyYAML>=3.11',
          ],
      scripts=[
          "run_pi_node.py",
          ])
