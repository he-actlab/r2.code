bench=$1
if [[ "$bench" == "zxing" || "$bench" == "jmeint" ]]
then
#	./runner-enerj.pl -D="chord.scope.exclude=java.,com.sun.,sun.,sunw.,javax.,launcher.,org." -D="chord.check.exclude=java.,com.sun.,sun.,sunw.,javax.,launcher.,org." -mode=parallel -workers=1 -analysis=expax -program=$1
	./runner-enerj.pl -D="chord.check.exclude=java.,com.sun.,sun.,sunw.,javax.,launcher.,org." -mode=parallel -workers=1 -analysis=expax -program=$1
else
#	./runner-enerj.pl -D="chord.scope.exclude=java.,com.,sun.,sunw.,javax.,launcher.,org." -D="chord.check.exclude=java.,com.,sun.,sunw.,javax.,launcher.,org." -mode=parallel -workers=1 -analysis=expax -program=$1
#	./runner-enerj.pl -D="chord.check.exclude=java.,com.,sun.,sunw.,javax.,launcher.,org." -mode=parallel -workers=1 -analysis=expax -program=$1
	./runner-enerj.pl -D="chord.check.exclude=java.,com.,sun.,sunw.,javax.,launcher.,org.,checker.,enerj." -D="chord.scope.exclude=checker.,enerj." -mode=parallel -workers=1 -analysis=expax -program=$1


fi


