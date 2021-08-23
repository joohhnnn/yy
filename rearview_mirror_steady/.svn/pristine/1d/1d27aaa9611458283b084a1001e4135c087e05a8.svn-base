
############# 别名定义 #############

#混乱字符
<~allChar>:(?:   (?:_[a-z]+\d_)  |  (?:[a-zA-Z]{1,3})  );
<allChaos>:(?: (?:<~allChar>)* );

#呼叫目标
<weChatTarget>:(?<toW> .+?);
<weChatGroupTarget>:(?<toGroup> .+?);
<shieldTarget>:(?<mask> .+?);
<shieldGroupTarget>:(?<maskG> .+?);
<sharePlaceTarget>:(?<shareP> .*?);
<sharePlaceGroupTarget>:(?<sharePG> .+?);
<lookTarget>:(?<lookT>.*?);
<unShieldTarget>:(?<umask> .*?);

<preActive>:我要|我想|我想要;
<prePassive>:帮我|替我|给我|帮忙|你给我|你替我;
<prePlease>:请|麻烦;
<preAllPls>:[<prePlease>]<prePassive>;
#<preAll>:<preActive>|<preAllPls>;
<preAll>:(?: .*? );

<weChatGroupSuffix>:群|群聊|讨论组|微信群;
<weChatGroupMsg>:微信群消息|群消息;
<weChatMsg>:微信|微信消息|微信信息;
<faceTarget>:(?<face>.+?);
<weChatMsgWithFace>:<weChatMsg>|表情;
<wechatAmount>:个|一个|条|一条;
<wechatTo>:给|到;
<sendWeChatMsg>:发送|发|回复|回;

<weChatGroupSimple>:群;

<shield>:屏蔽|关闭;
<shieldKw>:(?<umaskKw><shield>)
<sendMsg>:发来;
<receiveMsg>:信息|消息;
<keyWordDe>:的;
<speech>:播报

<share>:分享;
<own>:我的|当前|我当前的;
<dili>:地理
<place>:位置;
<wechPlace>:(?<wechP><place>);

<look>:查看|打开;
<lookHist>:(?<weHist><look>);
<history>:历史;
<appTag>:微信;

<cancel>:解除|取消;
############# 语法定义 #############

#发[一条]微信给XXX微信群
[<preAll>]<sendWeChatMsg>[<wechatAmount>]<weChatMsg><wechatTo><weChatGroupTarget><weChatGroupSuffix>;
#给xxx微信群发[一条]微信
[<preAll>]<wechatTo><weChatGroupTarget><weChatGroupSuffix><sendWeChatMsg>[<wechatAmount>]<weChatMsg>;

#给xxx微信群发[一条]群消息
[<preAll>]<wechatTo><weChatGroupTarget><weChatGroupSuffix><sendWeChatMsg>[<wechatAmount>]<weChatGroupMsg>;
#给xxx发[一条]微信群消息
[<preAll>]<wechatTo><weChatGroupTarget><sendWeChatMsg>[<wechatAmount>]<weChatGroupMsg>;

#发[一条]微信群消息给XXX微信群
[<preAll>]<sendWeChatMsg>[<wechatAmount>]<weChatGroupMsg><wechatTo><weChatGroupTarget><weChatGroupSuffix>;
#发[一条]微信群消息给XXX
[<preAll>]<sendWeChatMsg>[<wechatAmount>]<weChatGroupMsg><wechatTo><weChatGroupTarget>;

#给xxx发[一个][XXX]表情
[<preAll>]<wechatTo><weChatTarget><sendWeChatMsg>[<wechatAmount>][<faceTarget>]<weChatMsgWithFace>;
#发[一个]微信给XXX
[<preAll>]<sendWeChatMsg>[<wechatAmount>][<faceTarget>]<weChatMsgWithFace><wechatTo><weChatTarget>;

#(我要)屏蔽XXX发来的(群)信息
[<preAll>]<shield><shieldGroupTarget>[<sendMsg>]<keyWordDe><weChatGroupSuffix>[<appTag>]<receiveMsg>[<speech>];
[<preAll>]<shield><shieldTarget>[<sendMsg>]<keyWordDe>[<appTag>]<receiveMsg>[<speech>];

#分享我的位置给XXX/分享当前位置给XXX/分享我当前的位置给XXX
[<preAll>]<share>[<own>][<dili>]<wechPlace>[<wechatTo>]<sharePlaceGroupTarget><weChatGroupSimple>;
[<preAll>]<share>[<own>][<dili>]<wechPlace>[<wechatTo>]<sharePlaceTarget>;

#查看XX的微信[历史]消息
[<preAll>]<lookHist><lookTarget>[<keyWordDe>]<appTag>[<history>]<receiveMsg>

#解除屏蔽/取消屏蔽/解除微信消息屏蔽/取消微信消息屏蔽
[<preAll>]<cancel><unShieldTarget>[<weChatMsg>][<keyWordDe>]<shieldKw>
