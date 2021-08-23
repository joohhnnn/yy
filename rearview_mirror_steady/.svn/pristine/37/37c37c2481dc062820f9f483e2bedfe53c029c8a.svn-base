
############# 别名定义 #############

<preActive>:我要|我想|我想要;
<prePassive>:帮我|替我|给我|帮忙|你给我|你替我;
<prePlease>:请|麻烦;
<preAllPls>:[<prePlease>]<prePassive>;
<preAll>:<preActive>|<preAllPls>;

<tarMake>:把|给;
<tarModify>:修改|改变|设置|更换|设定|改|换|设;
<tarAs>:为|成|成为|到;
<tarAmount>:一下|下|一个|个;
<tarMe>:我|新;
<tarHome>:家|住址;
<tarCompany>:公司|单位;
<tarOf>:的;
<tarAddr>:地址|地点|地方|位置;
<tarAt>:在|位于;
<tarThat>:那|那里|那边;
<tarNear>:附近|最近|就近|旁边|周围|周边;
<tarType>:(?<hm><tarHome>)|(?<cm><tarCompany>);
<tarTar>:(?<tar> .+?);

############# 语法定义 #############

//[[我[的]]家]在xxxx[那里][[的]附近]
[[<tarMe>[<tarOf>]]<tarType>]<tarAt><tarTar>[<tarThat>][[<tarOf>]<tarNear>];
//[[我[的]]家[的]]位置在xxxx[那里][[的]附近]
[[<tarMe>[<tarOf>]]<tarType>[<tarOf>]]<tarAddr><tarAt><tarTar>[<tarThat>][[<tarOf>]<tarNear>];
//修改[我[的]]家[[的]地址]为xxx[那里][[的]附近]
[<preAll>]<tarModify>[<tarMe>[<tarOf>]]<tarType>[[<tarOf>]<tarAddr>]<tarAs><tarTar>[<tarThat>][[<tarOf>]<tarNear>];
//[把][我[的]]家[[的]地址]修改为xxx[那里][[的]附近]
[<preAll><tarMake>][<tarMe>[<tarOf>]]<tarType>[[<tarOf>]<tarAddr>]<tarModify><tarAs><tarTar>[<tarThat>][[<tarOf>]<tarNear>];

//修改一下家的地址
[<preAll>]<tarModify>[<tarAmount>][<tarMe>[<tarOf>]]<tarType>[<tarOf>]<tarAddr>;
//给家换个新的地址
[<preAll>]<tarMake>[<tarMe>[<tarOf>]]<tarType><tarModify>[<tarAmount>][<tarMe><tarOf>]<tarAddr>;

//[[我[的]]家]在xxxx[那里][[的]附近]
[[[<tarMe>[<tarOf>]]<tarType>]<tarAt>]<tarTar>[<tarThat>][[<tarOf>]<tarNear>];
