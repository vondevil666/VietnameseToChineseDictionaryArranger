import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字典条目结构：第一层按词性分解，成为一个词性后面跟随多个释义和例句
 * 				第二层按照具体释义分解，将一个词性的多个释义拆分成独立的释义
 * 				第三层根据冒号将独立的释义分解为释义本体和(多条)例句，释义本体可以添加到最终的3列表格中
 * 				第四层将释义的(多条)例句分解为单条例句，每条例句含越语和汉语，可以添加到最终的3列表格中
 *
 * 以下代码可能使用了一些描述不准确的词，此处一一记录
 * entry，词条
 * paraphases，释义，包含原字典中一个词性下的多条释义
 * property，词性
 * SingleParaphase，单条释义
 * exampleSentence，例句，含越语和汉语
 * paraphaseDetail，单条释义中的汉语解释（非例句部分）
 */
public class DictionaryArranger {
	public ArrayList<String[]> finalList=new ArrayList<String[]>();

	public static void main(String[] args) {
		DictionaryArranger da=new DictionaryArranger();
		da.reg();
	}
	
	private void reg(){
		String target1="điêu trác đg 雕琢:điêu trác ngọc 雕琢玉器 t 狡猾,狡诈:quen thói điêu trác 习惯了狡诈điều d ①条款,条文,条例,条令:điều khoản chung 共同条款 cả nhà đã được đoàn viên 得以全家团聚②言语,话语:Nói điều hay, làm việc tốt. 说好话,做好事。 ③事情:Quí vị có thể làm được điều này. 各位可办成这事。④条,项";
		String target2="đỏ đèn đg 上灯,点灯:Làng xóm đã đỏ đèn. 乡村已点上了灯。 d 点灯 (时刻,时分）: Đi từ mờ sớm đến đỏ đèn mới về. 天蒙蒙 亮出门,到点灯时分才回来。";
		String target3="đoàn viên đg ① [旧] 团圆②团zi②团聚:cả nhà đã được đoàn viên 得以全家团聚 d 团员: họp đoàn viên đoàn chi đoàn支部团员会议";
		String wordProperty=" d | đg | t | đ | p | k | tr | c ";
		Pattern p=Pattern.compile(wordProperty);
		String[] splitedPartsWithoutProperty=p.split(target1);
		Matcher m=p.matcher(target1);
		String[] propertyArray=buildPropertyArray(m);
		//检查切分是否正确，词属性数量应该比切分的部分少1个
		checkSentenceDivisionValidation(splitedPartsWithoutProperty,propertyArray);
		//记录下词条的越语词头
		String vietEntry=splitedPartsWithoutProperty[0];
		//将多对词性和释义存放进数组列表
		ArrayList<String[]> property_ParaphasesPair=buildProperty_ParaphasesPair(splitedPartsWithoutProperty,propertyArray);
		//进入词性-多句释义这一层
        arrangePair(vietEntry,property_ParaphasesPair);
		
//		ArrayList<String> sentenceAllParts=buildSentenceAllPartsList(splitedParts,propertyList);
		//finalList.addAll(buildCurrentSentencePartsTable(m,splitedPartsWithoutProperty));

        for (String[] s : finalList) {
            System.out.println("");
            System.out.println(s[0]);
            System.out.println(s[1]);
            System.out.println(s[2]);
        }

	}

    private void addParaphaseDetailToFinalList(String vietEntry,String property,String paraphaseDetail) {
        String[] array={vietEntry,property,paraphaseDetail};
        finalList.add(array);
    }

    private void addExampleSentenceToFinalList(String vietnameseSentence, String ChineseSentence) {
        String[] array = {vietnameseSentence, "例句", ChineseSentence};
        finalList.add(array);
    }

	//从词条所有的词性-释义句对开始往深层处理
	private void arrangePair(String vietEntry,ArrayList<String[]> pairList){
		for(String[] array:pairList){
			String property=array[0];
			String[] singleParaphaseArray=splitToSingleParaphase(array[1]);
            for (String singleParaphase : singleParaphaseArray) {
                String[] paraphaseDetailAndExampleSentences = Pattern.compile(":").split(singleParaphase);
                addParaphaseDetailToFinalList(vietEntry, property, paraphaseDetailAndExampleSentences[0]);
                //得到多组越-汉例句
                if(paraphaseDetailAndExampleSentences.length==1)continue;   //如果没有例句，则继续下一个释义_例句句对
                //例句中如果含有多个例句，则切分成单个例句
                String[] singleExampleSentenceArray=getSingleExampleSentenceArray(paraphaseDetailAndExampleSentences[1]);

                //6.13从此处开始，上面的数组得到了一个一个的例句，下面需要将例句分割成越语-汉语句对
                for (String singleExampleSentence : singleExampleSentenceArray) {
                    String[] seperatedVietChnExampleSentence = seperateVietnameseAndChineseFromExampleSentence(singleExampleSentence);
                    addExampleSentenceToFinalList(seperatedVietChnExampleSentence[0],seperatedVietChnExampleSentence[1]);
                }
            }
        }
	}

    private String[] seperateVietnameseAndChineseFromExampleSentence(String singleExampleSentece) {
	    StringBuilder sb=new StringBuilder();
        String[] viet_chnSentencePair = new String[2];
        char[] sentenceArray=singleExampleSentece.toCharArray();
        int isChineseFlag=0;
        for (char c : sentenceArray) {
            if(isChineseFlag==0){
                if(isChinese(c)==1){
                    viet_chnSentencePair[0]=sb.toString();
                    sb=new StringBuilder();
                    isChineseFlag=1;
                }
            }
            sb.append(c);
        }
        viet_chnSentencePair[1]=sb.toString();
        return viet_chnSentencePair;
    }

    private int isChinese(char c) {
        if((c>=19968 && c<=40959) || (c>=65281 && c<=65381) || (c>=12289 && c<=12319))return 1;
        else return 0;
    }

    private String[] getSingleExampleSentenceArray(String multiExampleSentencePair) {
        Pattern p = Pattern.compile("[\u0030-\u0039\u0041-\u005a\u0061-\u007a\u00e0-\u017f\u1ea0-\u1ef1\u01b0\u01a1 \\-\\(\\),.!?;\"\']+[\u4e00-\u9fff][\u0030-\u0039\u4e00-\u9fff ,.，。;、!?\\(\\)]*");
        Matcher m = p.matcher(multiExampleSentencePair);
        ArrayList<String> arrayList = new ArrayList<String>();
        while (m.find()) {
            arrayList.add(m.group());
        }
        String[] array = new String[arrayList.size()];
        return arrayList.toArray(array);
    }

    private String[] splitToSingleParaphase(String paraphase){
		ArrayList<String> singleParaphase=new ArrayList<String>();
		Pattern p=Pattern.compile("[\u2460-\u2469].*?(?=[\u2460-\u2469]|$)");
		Matcher m=p.matcher(paraphase);
		boolean founded=false;
        while(m.find()){
            founded=true;
            String tmp=m.group();
            singleParaphase.add(tmp);
        }
        if(founded==false) singleParaphase.add(paraphase);
        String [] tmpArray=new String[singleParaphase.size()];
		return singleParaphase.toArray(tmpArray);
	}
	
	private ArrayList<String[]> buildProperty_ParaphasesPair(String[] parts,String[] properties){
		ArrayList<String[]> pairList=new ArrayList<String[]>();
		String[] currentArray;
        for(int i=1;i<parts.length;i++){
			currentArray=new String[2];
            currentArray[0]=properties[i-1];
			currentArray[1]=parts[i];
			pairList.add(currentArray);
		}
		return pairList;
	}
	
	private void checkSentenceDivisionValidation(String[] parts,String[] properties){
		if(parts.length!=properties.length+1){
			try{
				throw new Exception("原句切分后词性数量与片段数量不对应，词性数量应等于片段数量-1");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private ArrayList<String[]> buildCurrentSentencePartsTable(Matcher m,String[] splitedPartsWithoutProperty){
		ArrayList<String[]> currentSentencePartsTable=new ArrayList<String[]>();
		String[] tempCurrentRawTable=new String[22];
//		ArrayList<String> propertyList=buildPropertyArray(m);
//		int propertyNum=propertyList.size();
//		for(int i=1;i<=propertyNum;i++){
//
//		}
//		return currentSentencePartsTable;
        return null;
	}
	
	//获得原句中的词性集合
	private String[] buildPropertyArray(Matcher m){
		ArrayList<String> list=new ArrayList<String>();
		while(m.find()){
			list.add(m.group());
		}
		String[] tmpArray=new String[list.size()];
		return list.toArray(tmpArray);
	}

	//
	private ArrayList<String> buildSentenceAllPartsList(String[] splitedParts,ArrayList<String> propertyList){
		ArrayList<String> allPartsList=new ArrayList<String>();
		for(int i=0;i<splitedParts.length;i++){
			allPartsList.add(splitedParts[i]);
			try{
				allPartsList.add(propertyList.get(i));
			}catch(Exception e){}
		}
		return allPartsList;
	}
}
