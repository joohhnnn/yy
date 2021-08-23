
############# 别名定义 #############

#混乱字符
<~allChar>:(?:   (?:_[a-z]+\d_)  |  (?:[a-zA-Z]{1,3})  );
<allChaos>:(?: (?:<~allChar>)* );

<preMe>:这|朕|正|我;
<preNeed>:要|想|想要;
<wkkHelp>:帮|替|给|为|跟;
<preYou>:你|您;
<wkkBa>:把;
<wkkWakeupKw>:唤醒词|唤醒时|换信纸|换信时;
<wkkCmd>:取|起|换|更换|命|改|修改|重新取|你的|从新取;
<wkkAmount>:个|一|一个;
<wkkName>:名|名字|新名字|新名|名子;
<wkkFeng>:册封你|封你|册封以为|封以为;
<wkkAs>:叫做|为|叫|成|就叫;
<wkkPreName>:你好|您好;
<wkkNameValue>:(?<name>.+?);
<wkkKingValue>:(?<king>.+?);
<wkkSetValue>:(?<set>.+?);
#[[我[要]]帮你]取[个]名字为[你好]XXX
<wkkGrammarName>:[[<preMe>[<preNeed>]]<wkkHelp><preYou>]<wkkCmd>[<wkkAmount>]<wkkName><wkkAs>[<wkkPreName>]<wkkNameValue>;
#[我[要]]封你为XXX
<wkkGrammarKing>:[<preMe>[<preNeed>]]<wkkFeng><wkkAs><wkkKingValue>;
#[[[我[要]]帮你]把]唤醒词修改为XXX
<wkkGrammarSet1>:[[[<preMe>[<preNeed>]]<wkkHelp><preYou>]<wkkBa>]<wkkWakeupKw><wkkCmd><wkkAs>;
#[[我[要]]帮你]修改唤醒词为XXX
<wkkGrammarSet2>:[[<preMe>[<preNeed>]]<wkkHelp><preYou>]<wkkCmd><wkkWakeupKw><wkkAs>;

<wkkGrammarSet>:(?:<wkkGrammarSet1>|<wkkGrammarSet2>)<wkkSetValue>;

<wkkStart>:<wkkGrammarName>|<wkkGrammarKing>|<wkkGrammarSet>;



############# 语法定义 #############
<wkkStart>;
