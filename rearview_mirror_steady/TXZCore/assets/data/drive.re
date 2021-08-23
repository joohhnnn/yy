
############# 别名定义 #############

#混乱字符
<~allChar>:(?:   (?:_[a-z]+\d_)  |  (?:[a-zA-Z]{1,3})  );
<allChaos>:(?: (?:<~allChar>)* );

<preAll>:<preActive>|<preAllPls>;
<preAllPls>:[<prePlease>]<prePassive>;

<preActive>:我要|我想|我想要;
<prePassive>:帮我|替我|给我|帮忙|你给我|你替我;
<preTell>:告诉我|你告诉我;
<prePlease>:请|麻烦;

//导航目标，支持家或公司或POI列表或周边
<navTarget>:[(?<near><navNear>)[<navOf>]]<navTar>;

<navLocation>:[<me>](?<loca><navLocationWhere>);
<me>:我;
<navNav>:指引我|导航|领路|带我|领我|引我|带领|带领我;
<navLocationWhere>:在哪里;
<navWhere>:怎么走|怎么去|怎么到|在哪里|哪有|哪里有;
<navCmdWords>:到|去|回|至;
<navGoWords>:去|至;
<navToWords>:到|回;
<navSuffix>:去;
<navNear>:附近|最近|就近|旁边|周围|周边;
<navFind>:查找|查|查询|找|搜索|搜|查找一下|搜搜;
<navAmount>:下|一下|个|一个;
<navOf>:的;
<navTar>:(?<toD>.+?);
<nearEnd>:(?<endD>.+?);
<navAct>:加油|吃饭|停车|取钱|住店|住宿|开房|购物|买衣服|买东西|喝咖啡|看病|上厕所|上洗手间|看电影|按摩|唱歌|喝酒|上网|买药|保养|做保养|汽车保养|做汽车保养|修车|洗车|做头发|充电;
<navNearAct>:(?<actD><navAct>);
<navAround>:(?<toAr>.+?);
<passBy>:途经的|先去|经过|先走|途经;
<passAround>:沿途的|途中的|途中|沿途;
<passFrom>:从;
<passWay>:走;
<passTar>:(?<pass>.+?);
<passLine>:的路线;
<preWant>:我想知道去;
<preOne>:沿途搜索;

<nearBy>:顺便;
<nearTar>:(?<sb>.+?);
<endPoi>:终点|目的地;
<preSearch>:找找|搜一下|找一下|查一下|查查|搜搜|查找一下|查找;

<passDel>:删除;
<passNot>:不去;
<passPoi>:途经点;
<kwPassNot>:(?<pN><passNot>);
<kwPassDel>:(?<pD><passDel>);
<kwFirm>:知豆;
<firmTar>:(?<firm><kwFirm>);
<poiType>:维修站|经销商|充电桩|充电站;
<poiTypeTar>:(?<poi><poiType>);
<centPoi>:(?<cPoi>.+?);

<preNavHst>:打开|查看|查找|找一下|查看下|看一下|查一下|查看一下|查询;
<navHst>:导航记录|导航历史|历史导航|导航历史记录|历史目的地;
<tarHst>:(?<hst><navHst>);
<navAgo>:之前|以前;
<navHst2>:去过哪里|去过哪儿|去过的地方|去的地方;
<tarHst2>:(?<hst><navHst2>);
<navHave>:有没有;

############# 语法定义 #############
//[我]不去[XXX]了
[<preAll>][<me>]<kwPassNot><passTar>;
//删除途经点[XXX]
[<preAll>]<kwPassDel><passPoi>[<passTar>];

//XXX[的]经销商/维修站
[<preAll>][<navNear>[<navOf>]]<firmTar>[<navOf>]<poiTypeTar>;
[<preAll>][<navFind>]<firmTar>[<navOf>]<poiTypeTar>;
[<preAll>][<navNav>][<navCmdWords>]<firmTar>[<navOf>]<poiTypeTar>;

//[帮我|我要]搜索|找|去途中的<POI>
[<preAll>]<navFind>[<navAmount>]<passAround><navAround>;
[<preAll>]<navGoWords><passAround><navAround>;

//查看导航记录
[<preAll>][<preNavHst>]<tarHst>;
//查看之前去过哪儿
[<preAll>]<preNavHst><navAgo><tarHst2>;

//[找一下]目的地[附近]的停车场
[<preAll>][<preSearch>]<endPoi>[<navNear>]<navOf><nearEnd>;
[<preAll>][<preSearch>]<endPoi>[<navNear>]<navHave><nearEnd>;
//[导航]去xxx[顺便XXX]
[<preAll>][<navNav>]<navCmdWords><navTarget><nearBy><nearTar>;
//[导航]去xxx
[<preAll>][<navNav>]<navGoWords><navTarget>;
//[导航]到xxx去
[<preAll>][<navNav>]<navToWords><navTarget><navSuffix>[<allChaos>];
//[告诉我]我在哪里
[[<prePlease>]<preTell>]<navLocation>[<allChaos>];
//[告诉我]xxx怎么走
[[<prePlease>]<preTell>]<navTarget><navWhere>[<allChaos>];
// 沿途搜索XXX
<passAround><navFind>[<navAmount>]<navAround>;
//我想知道沿途XXXX的路线
[<preWant>]<passAround><navAround><passLine>;
//[搜索][一下]沿途的XXX
[<preAll>][<navFind>][<navAmount>]<passAround><passTar>;
//[附近]搜索[一下][附近]有什么xxx
[<preAll>][<navNear>][<navFind>[<navAmount>]][<navNear>]<navWhere><navTarget>;
//[附近]搜索[一下][附近[的]]xxx[怎么走]
//[<preAll>][<navNear>]<navFind>[<navAmount>]<navTarget>[<navWhere>[<allChaos>]];

//我要xxx
<preAll><navNearAct>;

//（我要/我想）途径/先去/经过XXX
[<preActive>]<passBy><passTar>;
//从XXX走
[<preActive>]<passFrom><passTar><passWay>;
//走XXX
[<preActive>]<passWay><passTar>;



