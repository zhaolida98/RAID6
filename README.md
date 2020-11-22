# RAID6

In this project, we implement [RAID6](https://mirrors.edge.kernel.org/pub/linux/kernel/people/hpa/raid6.pdf) logic in Java. Before running the codes, you should first create several "disk folders" in your project, which 
represent to the real disks. Here is an example: 

```bash
cd RAID6/src/main/resources/
mkdir DiskGroups && cd DiskGroups
mkdir Disk1 Disk2 Disk3 Disk4 Disk5 Disk6 Disk7 Disk8
```

If you want to change the path to the disk groups, you can locate your own path and remember to update the variable value of *DISK_GROUP_ADDR* in 
*[Constants.java](https://github.com/zhaolida98/RAID6/blob/main/src/main/java/Constants.java)* before you run the codes. Now,  we will give a brief introduction of how you use these codes for testing RAID6 on your own computer. 

## Prerequisite

Our program was developed with Gradle 6.3. You can build it by command line if you have such environment. But we do recommend using `IntelleJ IDEA` as your IDE, since it has Gradle inside, which will make running the environment much easier. 

## Save File

Here we would like to introduce how we store and access abstract “data objects” with arbitrary size as multiple fix size chunks. The data chunk size is
defined as 4MB in *[Constants.java](https://github.com/zhaolida98/RAID6/blob/main/src/main/java/Constants.java)* file, named *CHUNK_SIZE*. 

Step 1:

prepare several files ready to be stored. To make it simple, we call our file `file1` and `file2`.

Step 2:

Look at the 11th line in `App.java`, you can put your prepared file's address here.

```java
List<String> targetFileList = Arrays.asList("file1", "file2");
```

Step 3:

run `App.java`, the system will chop the files and put all the chunks into disks. You can check the data chunk files in your "disk folder". You can also find the meta information for each data chunk file in `resources/Meta/meta.json`.

## Clear File

In `TestFilesSaver.java` we provide a function `cleanDisks()` which will help you clean all the data you stored previously.

## File Extraction

To check whether the chopping operation is correct, we also provide some file rebuild function in `FileExtractor.java`. You can test the correctness in `TestFileExtractor.java`. Of course we have done the test, it's bug free.

## Parity

The logic of parity is written in *[ParityGenerator.java](https://github.com/zhaolida98/RAID6/blob/main/src/main/java/ParityGenerator.java)* and *[ParityChecker.java](https://github.com/zhaolida98/RAID6/blob/main/src/main/java/ParityChecker.java)*. The former one is used for generating **P** and **Q** while the latter one are used to check whether it incurs some corruptions. If you run `App.java` directly, after saving files to the disk, the system will then automatically start to calculate the parites:

```java
ParityGenerator parityGenerator = new ParityGenerator();
parityGenerator.generate();
parityGenerator.storeParities();
```



### Parity Generation

In every disk file, we have already created several data chunks, which is mapped to the memory in real life. For example, the first file (not the first one in
the file system) represents the memory from 0 to 4MB and the second file represents the memory from 4MB to 8MB. Thus, we also use chunk logic to generate **P** and **Q**. 

We first get the first file of all disks, and their XOR result is $P_0$; since we assume that we are testing our algorithm on a 64-bit machine, we use
the following codes to generate **Q** of multiplication of {02} for a 8 byte value (c version). Thus, we finally can generate **Q0** by concatenate all 8-byte 
values together. For the other **Pi** and **Qi**, it is the same process. 

```c
uint64_t multiply02(uint64_t v) {
  uint64_t vv = (v << 1) & 0xfefefefefefefefe;
  vv ^= mask(v) & 0x1d1d1d1d1d1d1d1d;
  return vv;
}
```
```c
uint64_t mask(uint64_t v) {
  v &= 0x8080808080808080;
  return (v << 1) - (v >> 7);
}
```

The other codes are easy to read because it has already well commented. 

### Parity Checking

The checking process is much more complex than parity generation since there are 4 cases. Before detecting whether there are some corruptions, we need to use
the chunks in the data disks to generate current **P'** and **Q'**. This generation is the same as the parity generation. Now, we assume that we have already 
get **P'** and **Q'** and begin to discuss 4 cases (here we consider there is only one arbitrary corruption on an unknown disk).

- Case 1: No corruption

  This case should be the most common situation. We first get each data chunk from P drive (the drive stores the **P**) and XOR **P'**; and then we get each data
  chunk from Q drive (the drive stores the **Q**) and XOR **Q'**. If these two results are both zeros, then there is no corruption. 

- Case 2: P drive corruption

  We also XOR **P'** with **P** (assume that the result is **P***) and XOR **Q'** with **Q** (assume the result is **Q***). If **P*** is not zero but **Q*** is 
  zero, then P drive incurs corruption. To recovery, we just rerun the generation process to rebuild P drive. 

- Case 3: Q drive corruption

  Assume that we have already have **P*** and **Q***. If **P*** is zero but **Q*** is not zero, then Q drive incurs corruption. To recovery, we just rerun the 
  generation process to rebuild Q drive. 

- Case 4: Data drive corruption

  If both **P*** and **Q*** are not zero, then a data drive incurs corruption. To determine which data drive incurs, we use the algorithm in the manual book
  [RAID6](https://mirrors.edge.kernel.org/pub/linux/kernel/people/hpa/raid6.pdf), the detail of which will be presented in our report. After determine the 
  failure, we could recovery the corresponding disk by XOR the data chunks in P drive and all other data disks. 

### Simulation

We try to simulate the data error in arbitrary disks.  You first need to store files in to disks by running `App.java`, and then proceed the simulation

Simulation functions are listed in *[TestParity.java](https://github.com/zhaolida98/RAID6/blob/main/src/test/java/TestParity.java)*. There are three functions. After saving the files and generate parity **P** and **Q**, we need to use the function `simulateCorruption()` to simulate different corruptions on arbitrary disks. You can change the mode by commenting and uncommenting line 27-29 (already well commented). Finally, after simulating the corruption, we run the last function `test_checkParity()` to determine the failure and recover. After recovery, we can rerun the last function to check whether it is correct ("No corruption" in the console). If so, it means we have already determine and fixed the failure. 


