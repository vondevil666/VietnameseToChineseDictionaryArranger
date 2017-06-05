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
 * PreciceParaphase，单条释义
 * exampleSentence，例句，含越语和汉语
 * paraphaseDetail，单条释义中的汉语解释（非例句部分）
 */
public class DictionaryArranger {
	public ArrayList<String[]> finalList;

	public static void main(String[] args) {
		DictionaryArranger da=new DictionaryArranger();
		da.reg();
	}
	
	private void reg(){
		String target1="điêu trác đg 雕琢:điêu trác ngọc 雕琢玉器 t 狡猾,狡诈:quen thói điêu trác 习惯了狡诈điều d ①条款,条文,条例,条令:điều khoản chung 共同条款②言语,话语:Nói điều hay, làm việc tốt. 说好话,做好事。 ③事情:Quí vị có thể làm được điều này. 各位可办成这事。④条,项:10 điều nên";
		String target2="đỏ đèn đg 上灯,点灯:Làng xóm đã đỏ đèn. 乡村已点上了灯。 d 点灯 (时刻,时分）: Đi từ mờ sớm đến đỏ đèn mới về. 天蒙蒙 亮出门,到点灯时分才回来。";
		String target3="đoàn viên đg ① [旧] 团圆②团聚:cả nhà đã được đoàn viên 得以全家团聚 d 团员: họp đoàn viên đoàn chi đoàn支部团员会议";
		String wordProperty=" d | đg | t | đ | p | k | tr | c ";
		Pattern p=Pattern.compile(wordProperty);
		String[] splitedPartsWithoutProperty=p.split(target3);
		Matcher m=p.matcher(target3);
		String[] propertyArray=buildPropertyArray(m);
		//检查切分是否正确，词属性数量应该比切分的部分少1个
		checkSentenceDivisionValidation(splitedPartsWithoutProperty,propertyArray);
		//记录下词条的越语词头
		String vietEntry=splitedPartsWithoutProperty[0];
		//将多对词性和释义存放进数组列表
		ArrayList<String[]> property_ParaphasesPair=buildProperty_ParaphasesPair(splitedPartsWithoutProperty,propertyArray);
		//进入词性-多句释义这一层
		arrangePair(property_ParaphasesPair);
		
//		ArrayList<String> sentenceAllParts=buildSentenceAllPartsList(splitedParts,propertyList);
		finalList.addAll(buildCurrentSentencePartsTable(m,splitedPartsWithoutProperty));


	}

	//从词条所有的词性-释义句对开始往深层处理
	private void arrangePair(ArrayList<String[]> pairList){
		for(String[] array:pairList){
			String property=array[0];
			String[] preciceParaphaseArray=splitToPreciceParaphase(array[1]);
            for (String s : preciceParaphaseArray) {
                String[] paraphaseDetailAndExampleSentences = Pattern.compile(":").split(s);

            }
        }
	}
	
	private String[] splitToPreciceParaphase(String paraphase){
		ArrayList<String> preciceParaphase=new ArrayList<String>();
		Pattern p=Pattern.compile("[\\x{2460}-\\x{2469}].*?(?=[\\x{2460}-\\x{2469}]|\\r)");
		Matcher m=p.matcher(paraphase);
		while(m.find()){
			preciceParaphase.add(m.group());
		}
		String [] tmpArray=new String[preciceParaphase.size()];
		return preciceParaphase.toArray(tmpArray);
	}
	
	private ArrayList<String[]> buildProperty_ParaphasesPair(String[] parts,String[] properties){
		ArrayList<String[]> pairList=new ArrayList<String[]>();
		String[] currentArray;
		for(int i=1;i<=parts.length;i++){
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
		ArrayList<String> propertyList=buildPropertyList(m);
		int propertyNum=propertyList.size();
		for(int i=1;i<=propertyNum;i++){
			
		}
		return currentSentencePartsTable;
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
