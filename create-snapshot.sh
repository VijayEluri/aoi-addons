#!/bin/bash

# PLEASE MAKE SURE TO EXECUTE THIS SCRIPT DIRECTLY FROM ITS DIRECTORY!

function die()
{
	echo "Died: $@"
	exit 1
}

# Decide where to put the stuff
BASE="${PWD}/archives"

# Note that the names "Plugins" and "Scripts" are used elsewhere, too.
PDIR="${BASE}/Plugins"
SDIR="${BASE}/Scripts"

# Stop if there's an existing archive set
if [[ -d "$PDIR" ]]
then
	die "Target directory $PDIR exists. Please remove it."
fi

if [[ -d "$SDIR" ]]
then
	die "Target directory $SDIR exists. Please remove it."
fi

mkdir -p "$PDIR" || die "Could not create ${PDIR}"
mkdir -p "$SDIR" || die "Could not create ${SDIR}"

# See which directories can build
echo "*** Building plugins."
for i in Plugins/*
do
	if [[ -f "$i"/build.xml ]] && [[ -f "$i"/xml/extensions.xml ]]
	then
		# Okay, build it.
		cd "$i"
		ant || die "$i"

		# Find the current version
		VER=$(grep "^<extension name" xml/extensions.xml    \
				| sed 's/.*version="\(.*\)".*/\1/')

		# Find the actual jar-name
		JAR=$(basename antDist/*jar)

		# Save name and version in index
		echo -e "${VER}\t${JAR}" >> "$PDIR/INDEX"

		# Copy it
		cp antDist/*jar "$PDIR" || die "Copy failed."

		# Okay, let's clean this one up
		ant clean || die "$i"

		# Go back
		cd -
	fi
done

echo "*** Packing plugins."
cd "${BASE}" || die "Could not change directory to ${BASE}"
tar -czvf "Plugins-$(date +'%Y-%m-%d').tgz" Plugins/    \
		|| die "TAR failed."
echo "*** Cleaning up plugins."
rm -Rvf Plugins/
cd -

echo "*** Copying scripts."
cp -R Scripts/ "${BASE}" || die "Could not copy Scripts to ${BASE}"
cd "${BASE}" || die "Could not change directory to ${BASE}"
echo "*** Finding versions."
(
	cd Scripts
	for i in $(find -iname "*.bsh")
	do
		VER=$(grep '<version>' "$i" |    \
				sed 's/.*<version>\(.*\)<\/version>.*/\1/')
		echo -e "${VER}\t${i}" >> "$SDIR/INDEX"
	done
)
echo "*** Packing scripts."
tar -czvf "Scripts-$(date +'%Y-%m-%d').tgz" Scripts/    \
		|| die "TAR failed."
echo "*** Cleaning up scripts."
rm -Rvf Scripts/
cd -

echo
echo "*** We're done. Your files reside in ${BASE}"
