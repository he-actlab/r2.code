bench=$1
if [[ "$bench" == "zxing" || "$bench" == "jmeint" || "$bench" == "boofcv" ]]
then
	./runner.pl -D="chord.check.exclude=java.,com.sun.,sun.,sunw.,javax.,launcher.,org." -mode=parallel -workers=1 -analysis=r2 -program=$1
else
	./runner.pl -D="chord.check.exclude=java.,com.,sun.,sunw.,javax.,launcher.,org." -mode=parallel -workers=1 -analysis=r2 -program=$1
fi


