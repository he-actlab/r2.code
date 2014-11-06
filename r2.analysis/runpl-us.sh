bench=$1
if [[ "$bench" == "zxing-r2" || "$bench" == "jmeint-r2" ]]
then
	./runner-us.pl -D="chord.check.exclude=java.,com.sun.,sun.,sunw.,javax.,launcher.,org." -mode=parallel -workers=1 -analysis=r2 -program=$1 > /dev/null
else
	./runner-us.pl -D="chord.check.exclude=java.,com.,sun.,sunw.,javax.,launcher.,org." -mode=parallel -workers=1 -analysis=r2 -program=$1 > /dev/null
fi


