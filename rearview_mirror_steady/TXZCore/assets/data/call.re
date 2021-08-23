
############# 别名定义 #############

#混乱字符
<~allChar>:(?:   (?:_[a-z]+\d_)  |  (?:[a-zA-Z]{1,3})  );
<allChaos>:(?: (?:<~allChar>)* );

#呼叫目标
<callTarget>:(?<toC> .+?);

<preActive>:我要|我想|我想要;
<prePassive>:帮我|替我|给我|帮忙|你给我|你替我;
<prePlease>:请|麻烦;
<preAllPls>:[<prePlease>]<prePassive>;
<preAll>:<preActive>|<preAllPls>;

<callActToSuf>:一|个|一个|打|打个|打一个|拨|拨个|拨一个|拨打|拨打个|拨打一个;
<callPhoneSuf>:电话;
<callCmd>:打电话给|打个电话给|打一个电话给|拨号给|呼叫|打给|电话给|联系;
<callMake>:拨打;
<callPro>:移动|联通|电信|铁通;
<callPhoneA>:号码|电话|电话号|电话号码;
<callPhoneZ>:座机|座机号|座机号码|固定电话|固话;
<callPhoneM>:手机|手机号|手机号码;
<callPhoneS>:短号|亲情号|亲情短号|亲情号码|集团号|集团号码|集团短号;
<callTo>:给;
<callOf>:的;

<callPhone>:(?:<callPhoneA>)|(?<phZ><callPhoneZ>)|(?<phM><callPhoneM>)|(?<phS><callPhoneS>)

############# 语法定义 #############
#给xxx的手机打个电话
[<preAll>]<callTo><callTarget>[[<callOf>]<callPhone>][(?<act><callActToSuf>)]<callPhoneSuf>[<allChaos>];
#打电话给xxx的手机
[<preAll>]<callCmd><callTarget>[[<callOf>]<callPhone>];
#打xxx的手机
[<preAll>]<callMake><callTarget>[[<callOf>]<callPhone>];

