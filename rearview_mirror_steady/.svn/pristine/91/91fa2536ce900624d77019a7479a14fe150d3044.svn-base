
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

<navNav>:导航|领路|带我|领我|引我|带领|带领我;
<navWhere>:怎么走|怎么去|怎么到|在哪里|哪有|哪里有;
<navCmdWords>:到|去|回|至;
<navToWords>:到|回;
<navSuffix>:去;
<navNear>:附近|最近|就近|旁边|周围|周边;
<navFind>:查找|查|查询|找|搜索|搜;
<navAmount>:下|一下|个|一个;
<navOf>:的;
<navTar>:(?<toD>.+?);
<navNearAct>:(?<actD>.+?);
<passBy>:途经|先去|经过;
<passFrom>:从;
<passWay>:走;
<passTar>:(?<pass>.+?);
############# 语法定义 #############

//导航去xxx
[<preAll>][<navNav>]<navCmdWords><navTarget>;
//导航到xxx去
[<preAll>][<navNav>]<navToWords><navTarget><navSuffix>[<allChaos>];
//告诉我xxx怎么走
[[<prePlease>]<preTell>]<navTarget><navWhere>[<allChaos>];
//[附近]搜索[一下][附近[的]]xxx[怎么走]
[<preAll>][<navNear>]<navFind>[<navAmount>]<navTarget>[<navWhere>[<allChaos>]];
//[附近]搜索[一下][附近]有什么xxx
[<preAll>][<navNear>][<navFind>[<navAmount>]][<navNear>]<navWhere><navTarget>;

//我要xxx
<preAll><navNearAct>;
//（我要/我想）途径XXX/（我要/我想）先去XXX/（我要/我想）经过XXX
[<preActive>]<passBy><passTar>;
[<preActive>]<passFrom><passTar><passWay>;
[<preActive>]<passWay><passTar>;
