#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <signal.h>
#include <sys/time.h>
#include <time.h>

#include "utils.h"

void init_sigaction(void(*handler)(int signal))
{
    struct sigaction act;

    act.sa_handler = handler; //设置处理信号的函数
    act.sa_flags  = 0;

    sigemptyset(&act.sa_mask);
    sigaction(SIGALRM, &act, NULL);//时间到发送SIGROF信号
    //LOGD("init_sigaction, %d", 1);
}

void txz_settimer(long time)
{
	//LOGD("xxx: %s", "123");

	struct itimerval val;

    val.it_value.tv_sec = 1; //1秒后启用定时器
    val.it_value.tv_usec = 0;//time * 1000;

    val.it_interval = val.it_value;

    setitimer(ITIMER_REAL, &val, NULL);
}
