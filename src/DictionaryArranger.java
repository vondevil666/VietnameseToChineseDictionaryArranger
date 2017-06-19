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
	public ArrayList<String> needManualProcessList=new ArrayList<String>();
	public ArrayList<String> sentenceWithEqualitySignList=new ArrayList<String>();
	private String currentTargetText;
//	private ArrayList<String> propertyList=buildPropertyList();    	//弃用，可删

	public static void main(String[] args) {
		DictionaryArranger da=new DictionaryArranger();
		da.arrangeSingleLine("đo đếm đg 测量;计算 đo đỏ t 红通通,红彤彤");
	}

	public ArrayList<String[]> arrangeFullText(String fullText) {
		String[] fullTextArray = fullText.split("\n");
		for (String s : fullTextArray) {
			arrangeSingleLine(s);
		}
		return finalList;
	}
	
	private void arrangeSingleLine(String targetText){
		currentTargetText=targetText;
		String wordProperty=" d | đg | t | đ | p | k | tr | c ";
		Pattern p=Pattern.compile(wordProperty);
		String[] splitedPartsWithoutProperty=p.split(targetText);
		Matcher m=p.matcher(targetText);
		String[] propertyArray=buildPropertyArray(m);
		//检查切分失败的情况（失败的切分结果和原句相同）,因为无法区分"xxx好吃的"和"xxx dg好吃的"两种情况，此外还有xxx=yyy的情况，全部放进未处理列表中
		if(splitedPartsWithoutProperty[0].equals(targetText)){
			if(targetText.contains("=")) sentenceWithEqualitySignList.add(targetText);
			else needManualProcessList.add(targetText);
			return;
		}

		//检查切分是否正确，词属性数量应该比切分的部分少1个
		checkSentenceDivisionValidation(splitedPartsWithoutProperty,propertyArray);
		//记录下词条的越语词头
		String vietEntry=splitedPartsWithoutProperty[0];

		//用正则检查词头是否含有“đoạn [汉]断”这种情况，如果有，则将当前句加入待手工处理列表中，结束当前句子处理
		if(hasChinese(vietEntry)){
			needManualProcessList.add(currentTargetText);
			return;
		}
//		System.out.println(vietEntry);    //打印所有可处理文本的词头
		//将多对词性和释义存放进数组列表
		ArrayList<String[]> property_ParaphasesPair=buildProperty_ParaphasesPair(splitedPartsWithoutProperty,propertyArray);
		//进入词性-多句释义这一层
        arrangePair(vietEntry,property_ParaphasesPair);
		
//		ArrayList<String> sentenceAllParts=buildSentenceAllPartsList(splitedParts,propertyList);
		//finalList.addAll(buildCurrentSentencePartsTable(m,splitedPartsWithoutProperty));


	}

    private void addParaphaseDetailToFinalList(String vietEntry,String property,String paraphaseDetail) {
        String[] array={vietEntry.trim(),property.trim(),paraphaseDetail.trim()};
        finalList.add(array);
    }

    private void addExampleSentenceToFinalList(String vietnameseSentence, String ChineseSentence) {
        String[] array = {vietnameseSentence.trim(), "例句/短语", ChineseSentence.trim()};
        finalList.add(array);
    }

	//从词条所有的词性-释义句对开始往深层处理
	private void arrangePair(String vietEntry,ArrayList<String[]> pairList){
		for(String[] array:pairList){
			String property=array[0];
			//按①②切分，放进以下数组
			String[] singleParaphaseArray=splitToSingleParaphase(array[1]);
            for (String singleParaphase : singleParaphaseArray) {
            	//将具体释义和例句切分
                String[] paraphaseDetailAndExampleSentences = Pattern.compile(":").split(singleParaphase);
                //head指①等标记
				String headlessParaphaseDetail = removeHeadFromeParaphaseDetail(paraphaseDetailAndExampleSentences[0].trim());

				//可能存在这种情况：đo đếm đg 测量;计算 đo đỏ t 红通通,红彤彤。两个句子没有分开。
				//如果没有分开，第二句的词头部分会跟着第一句的汉语释义，因此可以从headlessParaphaseDetail中检测出来
				//检测headlessParaphaseDetail是否含有越南语字符,如果有则将整句添加到待处理列表，并跳出当前句处理流程

				//另外还会有"đô hộ d [旧] 都护 (古官名）đg 都护统治"，这种情况，第二个词性dg与前面没有切分开。
				//碰到这样情况，和碰到上述两句未切分的情况一同处理。
				if(containsVietnamese(headlessParaphaseDetail)){
					needManualProcessList.add(currentTargetText);
					return;
				}
				else addParaphaseDetailToFinalList(vietEntry, property, headlessParaphaseDetail);
                //得到多组越-汉例句
                if(paraphaseDetailAndExampleSentences.length==1)continue;   //如果没有例句，则继续下一个释义_例句句对
                //例句中如果含有多个例句，则切分成单个例句
                String[] singleExampleSentenceArray=getSingleExampleSentenceArray(paraphaseDetailAndExampleSentences[1]);

                //上面的数组得到了一个一个的例句，下面将例句分割成越语-汉语句对
                for (String singleExampleSentence : singleExampleSentenceArray) {
                    String[] separatedVietChnExampleSentence = seperateVietnameseAndChineseFromExampleSentence(singleExampleSentence);
                    addExampleSentenceToFinalList(separatedVietChnExampleSentence[0],separatedVietChnExampleSentence[1]);
                }
            }
        }
	}

	private boolean containsVietnamese(String headlessParaphaseDetail) {
		Pattern p=Pattern.compile("[\u00e0-\u017f\u1ea0-\u1ef1]");
		Matcher m = p.matcher(headlessParaphaseDetail);
		if(m.find())return true;
		else return false;
	}

	//弃用
	private ArrayList<String> buildPropertyList(){
		ArrayList<String> propertyList = new ArrayList<String>();
		propertyList.add("d");
		propertyList.add("đg");
		propertyList.add("t");
		propertyList.add("đ");
		propertyList.add("p");
		propertyList.add("k");
		propertyList.add("tr");
		propertyList.add("c");
		return propertyList;
	}

	//!！注意，该函数废止，因为无法区分“abc 中文释义”与“abc dg中文释义”，因此两种情况全部留待手工处理
	//原始文本中有一部分越南语-汉语的句对，类似例句模式。该函数用正则表达式抽取越-汉句对，如果句对数量超过2，代表匹配错误，由调用者处理。
	private String[] separateToViet_ChnArrayUseReg(String Viet_ChnSentence) {
		ArrayList<String> separatedViet_Chn = new ArrayList<String>();
		Pattern p = Pattern.compile("([\u0030-\u0039\u0041-\u005a\u0061-\u007a\u00e0-\u017f\u1ea0-\u1ef1\u01b0\u01a1 \\-\\(\\),.!?;\"']+)([\u4e00-\u9fff][\u0030-\u0039\u4e00-\u9fff ,.，。;、!?\\(\\)]*)");
		Matcher m = p.matcher(Viet_ChnSentence);
		while (m.find()) {
			String tmp=m.group(1);
			System.out.println(m.group(1)+"|"+m.group(2));
			separatedViet_Chn.add(tmp);
		}
		String[] tmpArray=new String[separatedViet_Chn.size()];
		for (String s : separatedViet_Chn) {
			System.out.println(s);
		}
		return separatedViet_Chn.toArray(tmpArray);
	}

	private String removeHeadFromeParaphaseDetail(String paraphaseDetailWithHead) {
		try{
			char c = paraphaseDetailWithHead.charAt(0);
			if(c>='\u2460' && c<='\u2473') return paraphaseDetailWithHead.substring(1, paraphaseDetailWithHead.length());
			else return paraphaseDetailWithHead;
		}catch(Exception e){}
		return "";
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

	private boolean hasChinese(String s) {
		Pattern p=Pattern.compile("[\u4eff-\u9fff]");
		Matcher m = p.matcher(s);
		if(m.find())return true;
		else return false;
	}

	private String[] getSingleExampleSentenceArray(String multiExampleSentencePair) {
        Pattern p = Pattern.compile("[\u0030-\u0039\u0041-\u005a\u0061-\u007a\u00c0-\u017f\u1ea0-\u1ef1\u01b0\u01a1 \\-\\(\\),.!?;\"\']+[\u4e00-\u9fff][\u0030-\u0039\u4e00-\u9fff \\-——.，,。;、!?\\(\\)]*");
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
