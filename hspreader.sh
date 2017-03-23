#!/usr/bin/env bash
## Remove overlapped HSPs

INPUT_NAME=`basename $1`

java -jar hspf.jar -e 0 -filter -in $INPUT_NAME > $INPUT_NAME.filtered
echo "Remove redundant HSPs..."


echo "... Finished."

## Concatenate alignments

echo "Concatenate alignments..."
cat $INPUT_NAME.filtered | sort -k1,1 -k2,2 -k6,6 -k4,4n -k5,5n | awk -v OFS='\t' '{l[NR] = $0} END
{for(i=1; i<=NR; i++){
    split(l[i],curLine,"\t");
    if(i==1){
        id=curLine[1];
        chr=curLine[2];
        le=curLine[3];
        start=curLine[4];
        end=curLine[5];
        distance=curLine[5]-curLine[4]+1;
        strand=curLine[6];
        sstart = curline[7];
        send = curline[8]
        score=curLine[9];
        mm=curLine[12];
        gaps=curLine[13];
        qDistances = start";"end;
        sDistances = sstart";"send;
    }else if(i==NR){
        if(curLine[1]==id && curLine[2]==chr && curLine[6]==strand){
            score+=curLine[9];
            distance+=curLine[5]-curLine[4]+1;
            mm+=curLine[12];
            gaps+=curLine[13]
            qDistances = qDistance"|"start";"end;
            sDistances = sDistance"|"sstart";"send;
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
            strand=curLine[6];score=curLine[9];
            mm=curLine[12];
            gaps=curLine[13]
        }
    }
}}' > $INPUT_NAME.temp
echo "... Finished."



## Select the best ones per read and calculate the coverage
echo "Calculating coverage..."
java -jar hspf.jar -cov -in $INPUT_NAME.temp > $INPUT_NAME.coverage

echo "Removing temporary files..."
rm $INPUT_NAME.temp

echo "Finish."
