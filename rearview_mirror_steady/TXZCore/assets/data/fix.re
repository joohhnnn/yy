
############# 别名定义 #############

#混乱字符
<~allChar>:(?:   (?:_[a-z]+\d_)  |  (?:[a-zA-Z]{1,3})  );
<allChaos>:(?: (?:<~allChar>)* );

<keyWordYes1>:是;
<keyWordYes2>:而是;
<keyWordYesNum>:4;
<keyWordNoNum>:84;
<keyWordNoNum2>:54;
<keyWordNo>:不是;
<keyWordDe>:的;

<preYesNum>:<keyWordYes1>|<keyWordYes2>|<keyWordYesNum>;
<preYesCH>:<keyWordYes1>|<keyWordYes2>;
<preNo>:<keyWordNoNum>|<keyWordNoNum2>|<keyWordNo>;

<goodTarget>:(?<gT>.+?);
<badTarget>:(?<bT>.+?);
<goodWord>:(?<gW>.+?);
<badWord>:(?<bW>.+?);

<threeNumYes>:(?<gW>\d{3});
<threeNumNo>:(?<bW>\d{3});
<fourNumYes>:(?<gW>\d{4});
<fourNumNo>:(?<bW>\d{4});
############# 语法定义 #############

<preYesCH><goodWord><keyWordDe><goodTarget><keyWordNo><badWord><keyWordDe><badTarget>;
<keyWordNo><badWord><keyWordDe><badTarget><preYesCH><goodWord><keyWordDe><goodTarget>;

<preYesNum><threeNumYes><preNo><threeNumNo>;
<preYesNum><fourNumYes><preNo><fourNumNo>;
<preNo><threeNumNo><preYesNum><threeNumYes>;
<preNo><fourNumNo><preYesNum><fourNumYes>;

<preYesCH><goodWord><keyWordNo><badWord>;
<keyWordNo><badWord><preYesCH><goodWord>;
