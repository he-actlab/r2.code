bench=$1
if [[ "$bench" == "zxing" || "$bench" == "jmeint" ]]
then
	./runner-us.pl -D="chord.check.exclude=java.,com.sun.,sun.,sunw.,javax.,launcher.,org." -mode=parallel -workers=1 -analysis=r2 -program=$1 > /dev/null
else
	./runner-us.pl -D="chord.check.exclude=java.,com.,sun.,sunw.,javax.,launcher.,org." -mode=parallel -workers=1 -analysis=r2 -program=$1 > /dev/null
fi


