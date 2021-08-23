
############# 别名定义 #############

#混乱字符
#<~allChar>:(?:   (?:_[a-z]+\d_)  |  (?:[a-zA-Z]{1,3})  );
#<allChaos>:(?: (?:<~allChar>)* );

#<preActive>:我要|我想|我想要;
#<prePassive>:帮我|替我|给我|帮忙|你给我|你替我;
#<prePlease>:请|麻烦;
#<preAllPls>:[<prePlease>]<prePassive>;
#<preAll>:<preActive>|<preAllPls>;
#<novPre>:第;
#<partTar>:(?<part>.+?);
#<novPart>:部|步;
#<chaptersTar>:(?<chap>.+?);
#<novChapters>:章|张;
#<stageTar>:(?<stage>.+?);
#<novStage>:集|期|小节|节|回;
#<kwStage>:(?<kStage><novStage>);
#<novelTar>:(?<novel>.+?);
#<novCmd>:听|播放|播|放|来|点播|点|收听;
#<tShowTar>:(?<tShow>.+?);
#<tShowInc>:包含|关于|有关;
#<incTar>:(?<tInc>.+?);
#<tShowOf>:那;
#<tShowStage>:期|集;
#<seasonTar>:(?<season>.+?);
#<kwSeason>:季;
#<kwLast>:最后|最新;
#<kwInverse>:倒数第;
#<preSeason>:(?<lsea>(?:<kwInverse>|<kwLast>))|<novPre>;
#<preStage>:(?<lsta>(?:<kwInverse>|<kwLast>))|<novPre>;
#<prePart>:(?<lpar>(?:<kwInverse>|<kwLast>))|<novPre>;
#<preChapters>:(?<lcha>(?:<kwInverse>|<kwLast>))|<novPre>;


############# 语法定义 #############
#//播放XXX关于XXX那期
#[<preAll>]<novCmd><tShowTar><tShowInc><incTar><tShowOf><tShowStage>;
#//播放XXX第XXX季第XXX期
#[<preAll>]<novCmd><tShowTar><preSeason><seasonTar><kwSeason><preStage><stageTar><tShowStage>;

#//播放第XX部第XX章第XX节
#[<preAll>]<novCmd>[<prePart><partTar><novPart>][<preChapters><chaptersTar><novChapters>]<preStage><stageTar><kwStage>;
#[<preAll>]<novCmd><novelTar>[<prePart><partTar><novPart>][<preChapters><chaptersTar><novChapters>]<preStage><stageTar><kwStage>;
#//播放第XX部第XX章
#[<preAll>]<novCmd>[<prePart><partTar><novPart>]<preChapters><chaptersTar><novChapters>;
#[<preAll>]<novCmd><novelTar>[<prePart><partTar><novPart>]<preChapters><chaptersTar><novChapters>;
#//播放第XX部
#[<preAll>]<novCmd><prePart><partTar><novPart>;
#[<preAll>]<novCmd><novelTar><prePart><partTar><novPart>;
