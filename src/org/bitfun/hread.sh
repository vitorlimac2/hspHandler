#!/usr/bin/env bash

awk -v OFS='\t'
'{l[NR] = $0}END
{
    for(i=1; i<=NR; i++){
        split(l[i],curLine,"\t");
        if(i==1){
            id=curLine[1];
            chr=curLine[2];
            le=curLine[3];
            start=curLine[4];
            end=curLine[5];
            distance=curLine[5]-curLine[4]+1;
            strand=curLine[6];
            score=curLine[9];
            mm=curLine[12];
            gaps=curLine[13]
        }else if(i==NR){
            if(curLine[1]==id && curLine[2]==chr && curLine[6]==strand){
                score+=curLine[9];
                distance+=curLine[5]-curLine[4]+1;
                mm+=curLine[12];
                gaps+=curLine[13]
            }else{
                print id,le,chr,strand,score,distance,mm,gaps;
                id=curLine[1];
                chr=curLine[2];
                le=curLine[3];
                start=curLine[4];
                end=curLine[5];
                distance=curLine[5]-curLine[4]+1;
                strand=curLine[6];
                score=curLine[9];
                mm=curLine[12];
                gaps=curLine[13]
            }
            print id,le,chr,strand,score,distance,mm,gaps;
        }else{
            if(curLine[1]==id && curLine[2]==chr && curLine[6]==strand){
                score+=curLine[9];
                distance+=curLine[5]-curLine[4]+1;
                mm+=curLine[12];
                gaps+=curLine[13]
            }else{
                print id,le,chr,strand,score,distance,mm,gaps;
                id=curLine[1];
                chr=curLine[2];
                le=curLine[3];
                start=curLine[4];
                end=curLine[5];
                distance=curLine[5]-curLine[4]+1;
                strand=curLine[6];
                score=curLine[9];
                mm=curLine[12];
                gaps=curLine[13]
            }
        }
    }
}'