import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

const val HARDCODED_CHARS = false
const val MUTATE = false
const val DESIRED_EXAMPLES = 5

val lenientJson = Json { ignoreUnknownKeys = true }

@Serializable
data class Field(
  val value: String,
//  val order: Int,
)

@Serializable data class NoteResult(val noteId: Long, val fields: Map<String, Field>)

@Serializable data class Response<A>(val error: String?, val result: A?)

class AnkiClient {
  private val httpClient =
    HttpClient(CIO) {
      install(ContentNegotiation) {
        json(
          Json {
            prettyPrint = true
            isLenient = true
          }
        )
      }
    }

  private suspend fun post(jsonRequest: String): HttpResponse =
    httpClient
      .post("http://127.0.0.1:8765") {
        contentType(ContentType.Application.Json)
        this.setBody(jsonRequest)
      }
      .also { check(it.status == HttpStatusCode.OK) { "Error response: $it" } }

  suspend fun findNotes(query: String): Response<List<Long>> {
    val result =
      post(
        buildRequest(
            action = "findNotes",
            params = JsonObject(mapOf("query" to JsonPrimitive(query)))
          )
          .toString()
      )
    return lenientJson.decodeFromString(result.bodyAsText())
  }

  suspend fun getNotes(vararg noteIds: Long): Response<List<NoteResult>> {
    val response =
      post(
        buildRequest(
            action = "notesInfo",
            params = JsonObject(mapOf("notes" to JsonArray(noteIds.map { JsonPrimitive(it) })))
          )
          .toString()
      )
    return lenientJson.decodeFromString(response.bodyAsText())
  }

  suspend fun updateNote(noteId: Long, exampleWordField: String): Response<Boolean?> {
    val result =
      post(
        buildRequest(
            action = "updateNoteFields",
            params =
              JsonObject(
                mapOf(
                  "note" to
                    JsonObject(
                      mapOf(
                        "id" to JsonPrimitive(noteId),
                        "fields" to
                          JsonObject(
                            mapOf(
                              "Example Word" to JsonPrimitive(exampleWordField),
                            )
                          )
                      )
                    )
                )
              )
          )
          .toString()
      )
    return lenientJson.decodeFromString(result.bodyAsText())
  }
}

private val anki = AnkiClient()

fun buildRequest(action: String, params: JsonObject): JsonObject =
  JsonObject(
    mapOf(
      "action" to JsonPrimitive(action),
      "version" to JsonPrimitive(6),
      "params" to params,
    )
  )

val RTK: CharArray =
  "一二三四五六七八九十口日月田目古吾冒朋明唱晶品呂昌早旭世胃旦胆亘凹凸旧自白百中千舌升昇丸寸肘専博占上下卓朝嘲只貝唄貞員貼見児元頁頑凡負万句肌旬勺的首乙乱直具真工左右有賄貢項刀刃切召昭則副別丁町可頂子孔了女好如母貫兄呪克小少大多夕汐外名石肖硝砕砂妬削光太器臭嗅妙省厚奇川州順水氷永泉腺原願泳沼沖汎江汰汁沙潮源活消況河泊湖測土吐圧埼垣填圭封涯寺時均火炎煩淡灯畑災灰点照魚漁里黒墨鯉量厘埋同洞胴向尚字守完宣宵安宴寄富貯木林森桂柏枠梢棚杏桐植椅枯朴村相机本札暦案燥未末昧沫味妹朱株若草苦苛寛薄葉模漠墓暮膜苗兆桃眺犬状黙然荻狩猫牛特告先洗介界茶脊合塔王玉宝珠現玩狂旺皇呈全栓理主注柱金銑鉢銅釣針銘鎮道導辻迅造迫逃辺巡車連軌輸喩前煎各格賂略客額夏処条落冗冥軍輝運冠夢坑高享塾熟亭京涼景鯨舎周週士吉壮荘売学覚栄書津牧攻敗枚故敬言警計詮獄訂訃討訓詔詰話詠詩語読調談諾諭式試弐域賊栽載茂戚成城誠威滅減蔑桟銭浅止歩渉頻肯企歴武賦正証政定錠走超赴越是題堤建鍵延誕礎婿衣裁装裏壊哀遠猿初巾布帆幅帽幕幌錦市柿姉肺帯滞刺制製転芸雨雲曇雷霜冬天妖沃橋嬌立泣章競帝諦童瞳鐘商嫡適滴敵匕叱匂頃北背比昆皆楷諧混渇謁褐喝葛旨脂詣壱毎敏梅海乞乾腹複欠吹炊歌軟次茨資姿諮賠培剖音暗韻識鏡境亡盲妄荒望方妨坊芳肪訪放激脱説鋭曽増贈東棟凍妊廷染燃賓歳県栃地池虫蛍蛇虹蝶独蚕風己起妃改記包胞砲泡亀電竜滝豚逐遂家嫁豪腸場湯羊美洋詳鮮達羨差着唯堆椎誰焦礁集准進雑雌準奮奪確午許歓権観羽習翌曜濯曰困固錮国団因姻咽園回壇店庫庭庁床麻磨心忘恣忍認忌志誌芯忠串患思恩応意臆想息憩恵恐惑感憂寡忙悦恒悼悟怖慌悔憎慣愉惰慎憾憶惧憧憬慕添必泌手看摩我義議犠抹拭拉抱搭抄抗批招拓拍打拘捨拐摘挑指持拶括揮推揚提損拾担拠描操接掲掛捗研戒弄械鼻刑型才財材存在乃携及吸扱丈史吏更硬梗又双桑隻護獲奴怒友抜投没股設撃殻支技枝肢茎怪軽叔督寂淑反坂板返販爪妥乳浮淫将奨采採菜受授愛曖払広勾拡鉱弁雄台怠治冶始胎窓去法会至室到致互棄育撤充銃硫流允唆出山拙岩炭岐峠崩密蜜嵐崎崖入込分貧頒公松翁訟谷浴容溶欲裕鉛沿賞党堂常裳掌皮波婆披破被残殉殊殖列裂烈死葬瞬耳取趣最撮恥職聖敢聴懐慢漫買置罰寧濁環還夫扶渓規替賛潜失鉄迭臣姫蔵臓賢腎堅臨覧巨拒力男労募劣功勧努勃励加賀架脇脅協行律復得従徒待往征径彼役徳徹徴懲微街桁衡稿稼程税稚和移秒秋愁私秩秘称利梨穫穂稲香季委秀透誘稽穀菌萎米粉粘粒粧迷粋謎糧菊奥数楼類漆膝様求球救竹笑笠笹箋筋箱筆筒等算答策簿築篭人佐侶但住位仲体悠件仕他伏伝仏休仮伎伯俗信佳依例個健側侍停値倣傲倒偵僧億儀償仙催仁侮使便倍優伐宿傷保褒傑付符府任賃代袋貸化花貨傾何荷俊傍俺久畝囚内丙柄肉腐座挫卒傘匁以似併瓦瓶宮営善膳年夜液塚幣蔽弊喚換融施旋遊旅勿物易賜尿尼尻泥塀履屋握屈掘堀居据裾層局遅漏刷尺尽沢訳択昼戸肩房扇炉戻涙雇顧啓示礼祥祝福祉社視奈尉慰款禁襟宗崇祭察擦由抽油袖宙届笛軸甲押岬挿申伸神捜果菓課裸斤析所祈近折哲逝誓斬暫漸断質斥訴昨詐作雪録剥尋急穏侵浸寝婦掃当彙争浄事唐糖康逮伊君群耐需儒端両満画歯曲曹遭漕槽斗料科図用庸備昔錯借惜措散廿庶遮席度渡奔噴墳憤焼暁半伴畔判拳券巻圏勝藤謄片版之乏芝不否杯矢矯族知智挨矛柔務霧班帰弓引弔弘強弥弱溺沸費第弟巧号朽誇顎汚与写身射謝老考孝教拷者煮著箸署暑諸猪渚賭峡狭挟頬追阜師帥官棺管父釜交効較校足促捉距路露跳躍践踏踪骨滑髄禍渦鍋過阪阿際障隙随陪陽陳防附院陣隊墜降階陛隣隔隠堕陥穴空控突究窒窃窟窪搾窯窮探深丘岳兵浜糸織繕縮繁縦緻線綻締維羅練緒続絵統絞給絡結終級紀紅納紡紛紹経紳約細累索総綿絹繰継緑縁網緊紫縛縄幼後幽幾機畿玄畜蓄弦擁滋慈磁系係孫懸遜却脚卸御服命令零齢冷領鈴勇湧通踊疑擬凝範犯氾厄危宛腕苑怨柳卵留瑠貿印臼毀興酉酒酌酎酵酷酬酪酢酔配酸猶尊豆頭短豊鼓喜樹皿血盆盟盗温蓋監濫鑑藍猛盛塩銀恨根即爵節退限眼良朗浪娘食飯飲飢餓飾餌館餅養飽既概慨平呼坪評刈刹希凶胸離璃殺爽純頓鈍辛辞梓宰壁璧避新薪親幸執摯報叫糾収卑碑陸睦勢熱菱陵亥核刻該骸劾述術寒塞醸譲壌嬢毒素麦青精請情晴清静責績積債漬表俵潔契喫害轄割憲生星醒姓性牲産隆峰蜂縫拝寿鋳籍春椿泰奏実奉俸棒謹僅勤漢嘆難華垂唾睡錘乗剰今含貪吟念捻琴陰予序預野兼嫌鎌謙廉西価要腰票漂標栗慄遷覆煙南楠献門問閲閥間闇簡開閉閣閑聞潤欄闘倉創非俳排悲罪輩扉侯喉候決快偉違緯衛韓干肝刊汗軒岸幹芋宇余除徐叙途斜塗束頼瀬勅疎辣速整剣険検倹重動腫勲働種衝薫病痴痘症瘍痩疾嫉痢痕疲疫痛癖匿匠医匹区枢殴欧抑仰迎登澄発廃僚瞭寮療彫形影杉彩彰彦顔須膨参惨修珍診文対紋蚊斑斉剤済斎粛塁楽薬率渋摂央英映赤赦変跡蛮恋湾黄横把色絶艶肥甘紺某謀媒欺棋旗期碁基甚勘堪貴遺遣潰舞無組粗租狙祖阻査助宜畳並普譜湿顕繊霊業撲僕共供異翼戴洪港暴爆恭選殿井丼囲耕亜悪円角触解再講購構溝論倫輪偏遍編冊柵典氏紙婚低抵底民眠捕哺浦蒲舗補邸郭郡郊部都郵邦那郷響郎廊盾循派脈衆逓段鍛后幻司伺詞飼嗣舟舶航舷般盤搬船艦艇瓜弧孤繭益暇敷来気汽飛沈枕妻凄衰衷面麺革靴覇声眉呉娯誤蒸承函極牙芽邪雅釈番審翻藩毛耗尾宅託為偽畏長張帳脹髪展喪巣単戦禅弾桜獣脳悩厳鎖挙誉猟鳥鳴鶴烏蔦鳩鶏島暖媛援緩属嘱偶遇愚隅逆塑遡岡鋼綱剛缶陶揺謡鬱就蹴懇墾貌免逸晩勉象像馬駒験騎駐駆駅騒駄驚篤罵騰虎虜膚虚戯虞慮劇虐鹿麓薦慶麗熊能態寅演辰辱震振娠唇農濃送関咲鬼醜魂魔魅塊襲嚇朕雰箇錬遵罷屯且藻隷癒璽潟丹丑羞卯巳此柴些砦髭禽檎憐燐麟鱗奄庵掩悛駿峻竣犀皐畷綴鎧凱呑韮籤懺芻雛趨尤厖或兎也巴疋菫曼云莫而倭侠倦俄佃仔仇伽儲僑倶侃偲侭脩倅做冴凋凌凛凧凪夙鳳剽劉剃厭雁贋厨仄哨咎囁喋嘩噂咳喧叩嘘啄吠吊噛叶吻吃噺噌邑呆喰埴坤壕垢坦埠堰堵嬰姦婢婉娼妓娃姪嬬姥姑姐嬉孕孜宥寓宏牢宋宍屠屁屑屡屍屏嵩崚嶺嵌帖幡幟庖廓庇鷹庄廟彊弛粥挽撞扮捏掴捺掻撰揃捌按播揖托捧撚挺擾撫撒擢摺捷抉怯惟惚怜惇恰恢悌澪洸滉漱洲洵滲洒沐泪渾涜梁澱洛汝漉瀕濠溌湊淋浩汀鴻潅溢湛淳渥灘汲瀞溜渕沌濾濡淀涅斧爺猾猥狡狸狼狽狗狐狛獅狒莨茉莉苺萩藝薙蓑苔蕩蔓蓮芙蓉蘭芦薯菖蕉蕎蕗茄蔭蓬芥萌葡萄蘇蕃苓菰蒙茅芭苅葱葵葺蕊茸蒔芹苫蒼藁蕪藷薮蒜蕨蔚茜莞蒐菅葦迪辿這迂遁逢遥遼逼迄逗鄭隕隈憑惹悉忽惣愈恕昴晋晟暈暉旱晏晨晒晃曝曙昂昏晦膿腑胱胚肛脆肋腔肱胡楓楊椋榛櫛槌樵梯柑杭柊柚椀栂柾榊樫槙楢橘桧棲栖桔杜杷梶杵杖樽櫓橿杓李棉楯榎樺槍柘梱枇樋橇槃栞椰檀樗槻椙彬桶楕樒毬燿燎炬焚灸煽煤煉燦灼烙焔烹牽牝牡琳琉瑳琢珊瑚瑞玖瑛玲畢畦痒痰疹痔癌痺眸眩雉矩磐碇碧硯砥碗碍碩磯砺碓禦祷祐祇祢禄禎秤黍禿稔稗穣稜稀穆窺窄穿竃竪颯站靖妾衿袷袴襖笙筏簾箪竿箆箔笥箭筑篠纂竺箕笈篇筈簸粕糟糊籾糠糞粟繋綸絨絆緋綜紐紘纏絢繍紬綺綾絃縞綬紗舵聯聡聘耽耶蚤蟹蛋蟄蝿蟻蝋蝦蛸螺蝉蛙蛾蛤蛭蛎罫袈裟截哉詢諄讐諌諒讃訊訣詫誼謬訝諺誹謂諜註譬轟輔輻輯豹賎貰賑贖躓蹄蹟跨跪醤醍醐醇麹釦銚鋤鋸錐鍬鋲錫錨釘鑓鋒鎚鉦錆鍾鋏閃悶閤雫霞翰斡鞍鞭鞘鞄靭鞠顛穎頗頌頚餐饗蝕飴駕騨馳騙馴駁駈驢鰻鯛鰯鱒鮭鮪鮎鯵鱈鯖鮫鰹鰍鰐鮒鮨鰭鴎鵬鸚鵡鵜鷺鷲鴨鳶梟塵麒舅鼠鑿艘瞑暝坐朔曳洩彗慧爾嘉兇兜靄劫歎輿歪翠黛鼎鹵鹸虔燕嘗殆牌覗齟齬秦雀隼耀夷嚢暢廻欣毅斯匙匡肇麿叢肴斐卿翫於套叛尖壷叡酋鴬赫臥甥瓢琵琶叉乖畠圃丞亮胤疏膏魁馨牒瞥睾巫敦奎翔皓黎赳已棘祟甦剪躾夥鼾陀粁糎粍噸哩浬吋呎梵薩菩唖牟迦珈琲檜轡淵伍什萬邁燭逞燈裡薗鋪嶋峯埜龍寵聾慾嶽國脛勁祀祓躇壽躊饅嘔鼈".toCharArray()
val CHARS: CharArray =
  "一二三四五六七八九十口日月田目古吾冒朋明唱晶品呂昌早旭世胃旦胆亘凹凸旧自白百中千舌升昇丸寸肘専博上下卓朝嘲只貝唄貞員貼見児元頁頑凡負万句肌旬勺的首乙乱直具工真左右有賄貢項刀刃切召昭則副別丁町可頂子孔了女好如母貫兄呪克小少大大多夕汐外名石肖硝砕砂妬削光太器臭嗅妙省厚奇川州順水氷泉腺原願永泳沼沖汎江汰汁沙潮源活消況河泊湖測土吐圧埼垣填圭封涯寺時均火炎煩淡灯畑災灰占点照魚漁里黒墨鯉量厘埋同洞胴向尚字守完宣宵安宴寄富貯木林森桂柏枠梢棚杏桐植椅枯朴村相机本札暦案燥未昧末沫味妹朱株若草苦苛寛薄葉模漠墓暮膜苗兆桃眺犬状黙然荻狩猫牛特告先洗介界茶脊合塔王玉宝珠現玩狂旺皇呈全全栓理理主注柱金銑鉢銅釣針銘鎮道導辻迅造迫逃辺巡車連軌輸喩前煎各格賂略客額夏処条落冗冥軍輝運冠夢坑高享塾熟亭京涼景鯨舎周週士吉壮荘売学覚栄書津牧攻敗枚故敬言警計詮獄訂訃討訓詔詰話詠詩語読調談諾諭式試弐域賊栽載茂戚成城誠威滅減蔑桟銭浅止歩渉頻肯企歴武賦正証政定錠走超赴越是題堤建鍵延誕礎婿衣裁装裏壊哀遠猿初巾布帆幅帽幕幌錦市柿姉肺帯滞刺制製転芸雨雲曇雷霜冬天妖沃橋嬌立泣章競帝諦童瞳鐘商嫡適滴敵匕叱匂頃北背比昆皆楷諧混渇謁褐喝葛旨脂詣壱毎敏梅海乞乾腹複欠吹炊歌軟次茨資姿諮賠培剖音暗韻識鏡境亡盲妄荒望方妨坊芳肪訪放激脱説鋭曽増贈東棟凍妊廷染燃賓歳県栃地池虫蛍蛇虹蝶独蚕風己起妃改記包胞砲泡亀電竜滝豚逐遂家嫁豪腸場湯羊美洋詳鮮達羨差着唯堆椎誰焦礁集准進雑雌準奮奪確午許歓権観羽習翌曜濯曰困固錮国団因姻咽園回壇店庫庭庁床麻磨心忘恣忍認忌志誌芯忠串患思恩応意臆想息憩恵恐惑感憂寡忙悦恒悼悟怖慌悔憎慣愉惰慎憾憶惧憧憬慕添必泌手看摩我義議犠抹拭拉抱搭抄抗批招拓拍打拘捨拐摘挑指持拶括揮推揚提損拾担拠描操接掲掛捗研戒弄械鼻刑型才財材存在乃携及吸扱丈史吏更硬梗又双桑隻護獲奴怒友抜投没股設撃殻支技枝肢茎怪軽叔督寂淑反坂板返販爪妥乳浮淫将奨采採菜受授愛曖払広勾拡鉱弁雄台怠治冶始胎窓去法会至室到致互棄育撤充銃硫流允唆出山拙岩炭岐峠崩密蜜嵐崎崖入込分貧頒公松翁訟谷浴容溶欲裕鉛沿賞党堂常裳掌皮波婆披破被残殉殊殖列裂烈死葬瞬耳取趣最撮恥職聖敢聴懐慢漫買置罰寧濁環還夫扶渓規替賛潜失鉄迭臣姫蔵臓賢腎堅臨覧巨拒力男労募劣功勧努勃励加賀架脇脅協行律復得従徒待往征径彼役徳徹徴懲微街桁衡稿稼程税稚和移秒秋愁私秩秘称利梨穫穂稲香季委秀透誘稽菌萎米粉粘粒粧迷粋謎糧菊奥数楼類漆膝様求球救竹笑笠笹箋筋箱筆筒等算答策簿築篭人佐侶但住位仲体悠件仕他伏伝仏休仮伎伯俗信佳依例個健側侍停値倣傲倒偵僧億儀償仙催仁侮使便倍優伐宿傷保褒傑付符府任賃代袋貸化花貨傾何荷俊傍俺久畝囚内丙柄肉腐座挫卒傘匁以似併瓦瓶宮営善膳年夜液塚幣蔽弊喚換融施旋遊旅勿物易賜尿尼尻泥塀履屋握屈掘堀居据裾層局遅漏刷尺尽沢訳択昼戸肩房扇炉戻涙雇顧啓示礼祥祝福祉社視奈尉慰禁襟宗崇祭察擦由抽油袖宙届笛軸甲押岬挿申伸神捜果菓課裸斤析所祈近折哲逝誓斬暫漸断質斥訴詐作雪録剥尋急穏侵浸寝婦掃当彙昨争浄事唐糖康逮伊君群耐需儒端両満画歯曲曹遭漕槽斗料科図用庸備昔錯借惜措散廿庶遮席度渡奔噴墳憤焼暁半伴畔判拳券巻圏勝藤謄片版之乏芝不否杯矢矯族知智挨矛柔務霧班帰弓引弔弘強弥弱溺沸費第弟巧号朽誇顎汚与写身射謝老考孝教拷者煮著箸署暑諸猪渚賭峡狭挟頬追阜師帥官棺管父釜交効較校足促捉距路露跳躍践踏踪骨滑髄禍渦鍋過阪阿際障隙随陪陽陳防附院陣隊墜降階陛隣隔隠堕陥穴空控突究窒窃窟窪搾窯窮探深丘岳兵浜糸織繕縮繁縦緻線綻締維羅練緒続絵統絞給絡結終級紀紅納紡紛紹経紳約細累索総綿絹繰継緑縁網緊紫縛縄幼後幽幾機畿玄畜蓄弦擁滋慈磁系係孫懸遜却脚卸御服命令零齢冷領鈴勇湧通踊疑擬凝範犯氾厄危宛腕苑怨柳卵留瑠貿印臼毀興酉酒酌酎酵酷酬酪酢酔配酸猶尊豆頭短豊鼓喜樹皿血盆盟盗温蓋監濫鑑藍猛盛塩銀恨根即爵節退限眼良朗浪娘食飯飲飢餓飾餌館餅養飽既概慨平呼坪評刈刹希凶胸離璃殺爽純頓鈍辛辞梓宰壁璧避新薪親幸執摯報叫糾収卑碑陸睦勢熱菱陵亥核刻該骸劾述術寒塞醸譲壌嬢毒素麦青精請情晴清静責績積債漬表俵潔契喫害轄割憲生星醒姓性牲産隆峰蜂縫拝寿鋳籍春椿泰奏実奉俸棒謹僅勤漢嘆難華垂唾睡錘剰今含貪吟念捻琴陰予序預野乗兼嫌鎌謙廉西価要腰票漂標栗慄遷覆煙南楠献門問閲閥間闇簡開閉閣閑聞潤欄闘倉創非俳排悲罪輩扉侯喉候決快偉違緯衛韓干肝刊汗軒岸幹芋宇余除徐叙途斜塗束頼瀬勅疎辣速整剣険検倹重動腫勲働種衝薫病痴痘症瘍痩疾嫉痢痕疲疫痛癖匿匠医匹区枢殴欧抑仰迎登澄発廃僚瞭寮療彫形影杉彩彰彦顔須膨参惨修珍診文対紋蚊斑斉剤済斎粛塁楽薬率渋摂央英映赤赦変跡蛮恋湾黄横把色絶艶肥甘紺某謀媒欺棋旗期碁基甚勘堪貴遺遣潰舞無組粗租狙祖阻査助宜畳並普譜湿顕繊霊業撲僕共供異翼戴洪港暴爆恭選殿井丼囲耕亜悪円角触解再講購構溝論倫輪偏遍編冊柵典氏紙婚低抵底民眠捕哺浦蒲舗補邸郭郡郊部都郵邦那郷響郎廊盾循派脈衆逓段鍛后幻司伺詞飼嗣舟舶航舷般盤搬船艦艇瓜弧孤繭益暇敷来気汽飛沈枕妻凄衰衷面麺革靴覇声眉呉娯誤蒸承函極牙芽邪雅釈番審翻藩毛耗尾宅託為偽畏長張帳脹髪展喪巣単戦禅弾桜獣脳悩厳鎖挙誉猟鳥鳴鶴烏蔦鳩鶏島暖媛援緩属嘱偶遇愚隅逆塑遡岡鋼綱剛缶陶揺謡鬱就蹴懇墾貌免逸晩勉象像馬駒験騎駐駆駅騒駄驚篤罵騰虎虜膚虚戯慮劇虞虐鹿麓薦慶麗熊能態寅演辰辱震振娠唇農濃送関咲鬼醜魂魔魅塊襲嚇朕雰箇錬遵罷屯且藻隷癒璽潟丹丑羞卯巳此柴些砦髭禽檎憐燐丞舐鹵朔酋爾袱醤麟鱗奄庵掩騙悛駿峻竣呆噂茄溢犀皐畷綴鎧凱呑韮籤懺艸芻雛趨尤厖或兎也巴疋囁菫曼云莫而倭侠倦俄佃仔仇伽儲僑倶侃偲侭脩倅做冴凋凌唸凛凧凪夙鳳剽劉剃厭".toCharArray()

fun findReading(char: Char, reading: String): Pair<String, String>? {
  val matcher =
    """([^\[\]「」()、　; ,,.]*$char[^\[\]「」()、　; ,,.]*)\[([^]]+)]"""
      .trimIndent()
      .toRegex()
      .find(reading)
      ?: return null
  return matcher.groupValues[1] to matcher.groupValues[2]
}

fun format(charToRedact: Char, example: Pair<String, String>): String {
  val (word, reading) = example
  return word.replace(charToRedact, '■') + "[$reading]"
}

data class Example(
  val noteId: Long,
  val c: Char,
  val word: String,
  val pronunciation: String,
) {
  val exampleText: String
    get() = format(c, word to pronunciation.replace(";.*".toRegex(), ""))
}

suspend fun findExamples(
  c: Char,
  deckName: String,
  searchField: String,
  desired: Int = 5,
  mapperOverride: ((note: NoteResult) -> Example?)? = null
): List<Example> {
  if (desired <= 0) return emptyList()

  val noteIds: List<Long>? =
    anki
      .findNotes(
        """
      "deck:$deckName" $searchField:*$c*
    """
          .trim()
      )
      .result

  if (noteIds.isNullOrEmpty()) return emptyList()

  val notes = anki.getNotes(*noteIds.take(desired).toLongArray())
  check(notes.error == null) { "Error returned: $notes" }

  return notes.result!!.mapNotNull(
    mapperOverride
      ?: lit@{ note ->
        val field = note.fields[searchField]!!.value
        val (word, pronunciation) = findReading(c, field) ?: return@lit null
        Example(
          noteId = note.noteId,
          c = c,
          word = word,
          pronunciation = pronunciation,
        )
      }
  )
}

suspend fun main() {
  val chars: List<Pair<Long, Char>> = getChars()

  var total = 0
  val foundInN5 = mutableListOf<Char>()
  val foundInSentence = mutableListOf<Char>()
  val foundInDictionary = mutableListOf<Char>()
  val missing = mutableListOf<Char>()

  for ((cid: Long, c: Char) in chars) {
    total++
    val examples = mutableSetOf<String>()

    var a =
      findExamples(
        c,
        deckName = "JLPT Tango N5 (abandoned)",
        searchField = "Reading",
        desired = DESIRED_EXAMPLES - examples.size
      )
    if (a.isNotEmpty()) {
      examples.addAll(a.map { it.exampleText })
      foundInN5 += c
    }

    a =
      findExamples(
        c,
        deckName = "Sentence Cards",
        searchField = "Sentence",
        desired = DESIRED_EXAMPLES - examples.size
      )
    if (a.isNotEmpty()) {
      examples.addAll(a.map { it.exampleText })
      foundInSentence += c
    }

    a =
      findExamples(
        c,
        deckName = "Sentence Cards",
        searchField = "Expression",
        desired = DESIRED_EXAMPLES - examples.size
      )
    if (a.isNotEmpty()) {
      examples.addAll(a.map { it.exampleText })
      foundInSentence += c
    }

    a =
      findExamples(
        c,
        deckName = "Shinmeikai Definitions",
        searchField = "Word",
        desired = DESIRED_EXAMPLES - examples.size
      ) { note ->
        val field = note.fields["Full Definition"]!!.value
        val (word, pronunciation) =
          note.fields["Word"]!!.value to field.substring(0 until field.indexOf(" "))
        Example(noteId = note.noteId, c = c, word = word, pronunciation = pronunciation)
      }
    if (a.isNotEmpty()) {
      examples.addAll(a.map { it.exampleText })
      foundInDictionary += c
    }

    if (examples.isEmpty()) missing += c

    val exampleWordField: String = examples.joinToString(" 、 ")
    println("$c -> $exampleWordField")

    if (MUTATE && !HARDCODED_CHARS && examples.isNotEmpty()) {
      val updateResponse = anki.updateNote(noteId = cid, exampleWordField)
      check(updateResponse.error == null) { "Error while updating $cid: $updateResponse" }
    }
  }

  println("total: $total")
  println(
    "foundInSentence (${foundInSentence.size}): " + foundInSentence.joinToString(separator = " ")
  )
  println("foundInN5 (${foundInN5.size}): " + foundInN5.joinToString(separator = " "))
  println(
    "foundInDictionary (${foundInDictionary.size}): " +
      foundInDictionary.joinToString(separator = " ")
  )
  println("missing (${missing.size}): " + missing.joinToString(separator = " "))
}

val NO_COVERAGE =
  "吾 朋 晶 昌 旭 旦 亘 升 肘 唄 頁 旬 勺 貢 昭 孔 克 汐 硝 妬 腺 永 汎 迅 喩 煎 賂 冥 享 塾 鯨 壮 津 敬 詮 訃 詔 詠 諭 弐 栽 桟 肯 賦 堤 婿 帆 錦 柿 亀 逐 腸 羨 唯 堆 椎 礁 准 歓 錮 姻 恣 芯 臆 憩 寡 悦 悼 愉 惰 憾 惧 憬 泌 抹 抄 批 拓 拘 揮 殻 茎 叔 淑 妥 淫 奨 勾 冶 棄 硫 允 岐 峠 嵐 頒 翁 訟 陳 附 墜 陛 堕 窒 窟 窪 搾 窯 丘 岳 繕 綻 維 羅 紡 累 索 紫 刈 刹 璃 頓 梓 宰 糾 睦 菱 陵 亥 劾 壌 漬 契 轄 憲 峰 鋳 籍 椿 俸 謹 僅 嘆 錘 剰 貪 吟 琴 序 謙 廉 票 慄 遷 楠 献 閲 閥 閑 倹 勲 痘 痩 嫉 痢 疫 枢 瞭 療 杉 彰 紋 蚊 斉 粛 摂 央 赦 某 謀 碁 甚 堪 査 顕 戴 洪 耕 亜 呉 娯 函 翻 耗 脹 鶴 蔦 鳩 媛 嘱 塑"
    .splitToSequence(" ")
    .toSet()

private suspend fun getChars(): List<Pair<Long, Char>> =
  if (HARDCODED_CHARS) {
    CHARS.map { -1L to it }
  } else {
    buildList {
      val noteIds = anki.findNotes("deck:RTK").result!!
      for (noteId in noteIds) {
        val response = anki.getNotes(noteId)
        val content = response.result!!.single().fields["Character"]!!.value

        check(content.length == 1) { "Not one char! '$content'" }

        add(noteId to content.single())
      }
      //      File("tmp.txt").writeText(this.map { it.second }.joinToString(""))
      //      println("chars: " + this.map { it.second }.joinToString(""))
    }
  }
