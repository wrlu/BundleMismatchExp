#  Bundle Mismatch Exploit
## 漏洞分析
### [hw 0 day] com.huawei.recsys.aidl.HwObjectContainer
```java=
protected HwObjectContainer(Parcel arg2) {
    this.clazz = (Class)arg2.readSerializable();
    Class v0 = this.clazz;
    if(v0 != null) {
        this.objects = arg2.readArrayList(v0.getClassLoader());
        return;
    }
    this.objects = Collections.emptyList();
}

@Override  // android.os.Parcelable
public void writeToParcel(Parcel dest, int flags) {
    dest.writeSerializable(this.clazz);
    dest.writeList(this.objects);
}
```

### [AOSP 0 day] android.os.WorkSource
```java=
@UnsupportedAppUsage
WorkSource(Parcel in) {
    this.mNum = in.readInt();
    this.mUids = in.createIntArray();
    this.mNames = in.createStringArray();
    int numChains = in.readInt(); // numChains = 1
    if (numChains > 0) {
        this.mChains = new ArrayList<>(numChains); // create length = 1 array list
        in.readParcelableList(this.mChains, WorkChain.class.getClassLoader()); // read empty Parcelable list
        return;
    }
    this.mChains = null;
}

@Override // android.os.Parcelable
public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(this.mNum);
    dest.writeIntArray(this.mUids);
    dest.writeStringArray(this.mNames);
    ArrayList<WorkChain> arrayList = this.mChains;
    if (arrayList == null) {
        dest.writeInt(-1);
        return;
    }
    dest.writeInt(arrayList.size()); // write size is 0
    dest.writeParcelableList(this.mChains, flags); // write int 0
}
```
这个还是size处理的问题
第一次read的时候，numChains写1，然后进到里面readParcelableList直接放一个-1代表null，这样就给mChains创建了一个长度为1的ArrayList，但是并未add任何成员，这里面读取了2个int，8个字节
然后进行write的时候，写入的是arrayList.size()，这里面会写入0，writeParcelableList也会写入0，因为this.mChains里面并没有内容，但不是null
第二次read的时候，numChains是0，不进入分支，不调用readParcelableList，仅读取1个int，4个字节
日志：
```
bundle key is ����������������������
bundle key hash code is -581264888
bundle value is WorkSource{ chains=}
bundle key is ��D����������������
bundle key hash code is 146422115
bundle value is 1
bundle key is ������������������������
bundle key hash code is 2135068438
bundle value len is 972

badBundle key is intent
badBundle key hash code is -1183762788
badBundle value is Intent { flg=0x40008000 cmp=com.huawei.intelligent/com.huawei.hms.activity.BridgeActivity (has extras) }
badBundle key is ����������������������
badBundle key hash code is -581264888
badBundle value is WorkSource{}
badBundle key is 
badBundle key hash code is 0
badBundle value len is 68
```

## 漏洞利用
待补充
