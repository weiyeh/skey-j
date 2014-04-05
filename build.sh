ant
cd build
jar cfm ../skey.jar ../manifestcli.txt *
cd ..
if !([ -d ~/.skey ])
then
	mkdir ~/.skey
fi

cp skey.jar ~/.skey/

