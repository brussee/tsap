#!/bin/bash
echo "Remove old Tribler code"
rm -r Tribler-0.1.0/Tribler

echo "Copy new Tribler code"
cp -r tribler/Tribler Tribler-0.1.0/

echo "Remove pyc and pyo files"
pushd Tribler-0.1.0/Tribler
find -name '*.pyc' -exec echo {} \;
find -name '*.pyo' -exec echo {} \;
popd

echo "Make tarball"
tar cvzf Tribler-0.1.0.tar.gz Tribler-0.1.0

echo "MD5SUM: "
md5sum Tribler-0.1.0.tar.gz

echo "Copy to server"
scp Tribler-0.1.0.tar.gz fr:/var/www/default/tsap/