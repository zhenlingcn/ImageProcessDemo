package top.zhenlingcn.imageprocessdemo.WaterShed;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 *分水岭算法进行图像分割
 *@author zhshl
 *@date 2014-10-31
 */
public class WaterShed {

    private static final String TAG = "TAG";
    private Bitmap sourceImg;///原图
    private Bitmap gradImage;////灰度图

    /**
     * 	///此处采用数组实现指向父节点的多叉树，根节点的父节点是它自己（指向自己，下标用转化后的数值表示比如i行j列，下标为：i*width+j）
     *  ///用查集数据结构来进行区域标记，主要目的在于简化区域块的合并
     * 	////记录分块数据
     */
    private int [][] blockData;

    private int width;
    private int height;

    private final int THRESHOD=20;
    private int maxGrad;////最大梯度值，也是灌水灌到的最高点

    private int RIDGE=-100;////该标志表示山脊

    private HashMap<Integer,Integer> map=new HashMap<>();

    private ImageView imageView;

    /**
     * 开始进行分水岭算法分割图片
     * @param file
     */
    public void startWatering(File file,ImageView imageView){

        init(file);
        this.imageView=imageView;
        startWatering();
    }



    /**
     * 开始灌水生长
     */
    private void startWatering(){

        for(int altitude=THRESHOD;altitude<=maxGrad;altitude++){
            ////外层循环，表示每次灌水达到的高度
            int addedPixes=0;////记录每次循环有多少个像素点加入了集水盆，

            do{
                addedPixes=0;
                ////在某个高度下，一直循环到没有新像素点加入到集水盆为止
                for(int j=0;j<height;j++){
                    for(int i=0;i<width;i++){
                        ///内部循环遍历图片
                        int grad=(gradImage.getPixel(i, j)>>16)&0xFF;
                        if((grad<=altitude)&&(blockData[j][i]==-1)){
                            ////处理未标记的区域
                            int num=0;///记录四领域集水盆数目
                            int parentIndex=-1;


                            ////左边
                            if(i>0){////不超出边界
                                int value=blockData[j][i-1];
                                if(value!=-1&&value!=-100){
                                    ///把该集水盆的代表节点的父节点下标值记录下来

                                    ///找到左边区域的根节点即某节点指向自身
                                    int left_parentIndex=value;
                                    int left_j=left_parentIndex/width;///商表示行
                                    int left_i=left_parentIndex%width;///余数表示列

                                    while(blockData[left_j][left_i]!=left_parentIndex){
                                        left_parentIndex=blockData[left_j][left_i];
                                        left_j=left_parentIndex/width;///商表示行
                                        left_i=left_parentIndex%width;///余数表示列
                                    }
                                    parentIndex=left_parentIndex;
                                    num++;
                                }
                            }
                            ////右边
                            if(i<width-1){////不超出边界
                                int value=blockData[j][i+1];
                                if(value!=-1&&value!=-100){

                                    ////当该标记点所在区域的值与之前的区域值不一致时，四邻域集水盆数目加一

                                    ///找到左边区域的根节点即某节点指向自身
                                    int left_parentIndex=value;
                                    int left_j=left_parentIndex/width;///商表示行
                                    int left_i=left_parentIndex%width;///余数表示列

                                    while(blockData[left_j][left_i]!=left_parentIndex){
                                        left_parentIndex=blockData[left_j][left_i];
                                        left_j=left_parentIndex/width;///商表示行
                                        left_i=left_parentIndex%width;///余数表示列
                                    }
                                    if(left_parentIndex!=parentIndex){
                                        num++;
                                    }

                                    ///把该集水盆的代表节点的父节点下标值记录下来
                                    parentIndex=left_parentIndex;


                                }
                            }
                            ////上边
                            if(j>0){////不超出边界
                                int value=blockData[j-1][i];
                                if(value!=-1&&value!=-100){
                                    ////当该标记点所在区域的值与之前的区域值不一致时，四邻域集水盆数目加一
                                    ///找到左边区域的根节点即某节点指向自身
                                    int left_parentIndex=value;
                                    int left_j=left_parentIndex/width;///商表示行
                                    int left_i=left_parentIndex%width;///余数表示列

                                    while(blockData[left_j][left_i]!=left_parentIndex){
                                        left_parentIndex=blockData[left_j][left_i];
                                        left_j=left_parentIndex/width;///商表示行
                                        left_i=left_parentIndex%width;///余数表示列
                                    }
                                    if(left_parentIndex!=parentIndex){
                                        num++;
                                    }
                                    ///把该集水盆的代表节点的父节点下标值记录下来
                                    parentIndex=left_parentIndex;
                                }
                            }

                            ////下边
                            if(j<height-1){////不超出边界
                                int value=blockData[j+1][i];
                                if(value!=-1&&value!=-100){

                                    ////当该标记点所在区域的值与之前的区域值不一致时，四邻域集水盆数目加一
                                    ///找到左边区域的根节点即某节点指向自身
                                    int left_parentIndex=value;
                                    int left_j=left_parentIndex/width;///商表示行
                                    int left_i=left_parentIndex%width;///余数表示列

                                    while(blockData[left_j][left_i]!=left_parentIndex){
                                        left_parentIndex=blockData[left_j][left_i];
                                        left_j=left_parentIndex/width;///商表示行
                                        left_i=left_parentIndex%width;///余数表示列
                                    }
                                    if(left_parentIndex!=parentIndex){
                                        num++;
                                    }
                                    ///把该集水盆的代表节点的父节点下标值记录下来
                                    parentIndex=left_parentIndex;
                                }
                            }


                            ////左上
                            if(i>0&&j>0){////不超出边界
                                int value=blockData[j-1][i-1];
                                if(value!=-1&&value!=-100){

                                    ////当该标记点所在区域的值与之前的区域值不一致时，四邻域集水盆数目加一
                                    ///找到左边区域的根节点即某节点指向自身
                                    int left_parentIndex=value;
                                    int left_j=left_parentIndex/width;///商表示行
                                    int left_i=left_parentIndex%width;///余数表示列

                                    while(blockData[left_j][left_i]!=left_parentIndex){
                                        left_parentIndex=blockData[left_j][left_i];
                                        left_j=left_parentIndex/width;///商表示行
                                        left_i=left_parentIndex%width;///余数表示列
                                    }
                                    if(left_parentIndex!=parentIndex){
                                        num++;
                                    }
                                    ///把该集水盆的代表节点的父节点下标值记录下来
                                    parentIndex=left_parentIndex;
                                }
                            }


                            ////右上
                            if(i<width-1&&j>0){////不超出边界
                                int value=blockData[j-1][i+1];
                                if(value!=-1&&value!=-100){

                                    ////当该标记点所在区域的值与之前的区域值不一致时，四邻域集水盆数目加一
                                    ///找到左边区域的根节点即某节点指向自身
                                    int left_parentIndex=value;
                                    int left_j=left_parentIndex/width;///商表示行
                                    int left_i=left_parentIndex%width;///余数表示列

                                    while(blockData[left_j][left_i]!=left_parentIndex){
                                        left_parentIndex=blockData[left_j][left_i];
                                        left_j=left_parentIndex/width;///商表示行
                                        left_i=left_parentIndex%width;///余数表示列
                                    }
                                    if(left_parentIndex!=parentIndex){
                                        num++;
                                    }
                                    ///把该集水盆的代表节点的父节点下标值记录下来
                                    parentIndex=left_parentIndex;
                                }
                            }


                            ////左下
                            if(i>0&&j<height-1){////不超出边界
                                int value=blockData[j+1][i-1];
                                if(value!=-1&&value!=-100){

                                    ////当该标记点所在区域的值与之前的区域值不一致时，四邻域集水盆数目加一
                                    ///找到左边区域的根节点即某节点指向自身
                                    int left_parentIndex=value;
                                    int left_j=left_parentIndex/width;///商表示行
                                    int left_i=left_parentIndex%width;///余数表示列

                                    while(blockData[left_j][left_i]!=left_parentIndex){
                                        left_parentIndex=blockData[left_j][left_i];
                                        left_j=left_parentIndex/width;///商表示行
                                        left_i=left_parentIndex%width;///余数表示列
                                    }
                                    if(left_parentIndex!=parentIndex){
                                        num++;
                                    }
                                    ///把该集水盆的代表节点的父节点下标值记录下来
                                    parentIndex=left_parentIndex;
                                }
                            }


                            ////右下
                            if(i<width-1&&j<height-1){////不超出边界
                                int value=blockData[j+1][i+1];
                                if(value!=-1&&value!=-100){

                                    ////当该标记点所在区域的值与之前的区域值不一致时，四邻域集水盆数目加一
                                    ///找到左边区域的根节点即某节点指向自身
                                    int left_parentIndex=value;
                                    int left_j=left_parentIndex/width;///商表示行
                                    int left_i=left_parentIndex%width;///余数表示列

                                    while(blockData[left_j][left_i]!=left_parentIndex){
                                        left_parentIndex=blockData[left_j][left_i];
                                        left_j=left_parentIndex/width;///商表示行
                                        left_i=left_parentIndex%width;///余数表示列
                                    }
                                    if(left_parentIndex!=parentIndex){
                                        num++;
                                    }
                                    ///把该集水盆的代表节点的父节点下标值记录下来
                                    parentIndex=left_parentIndex;
                                }
                            }




                            ////领域集水盆数目刚好等于1个
                            if(num==1){
                                ////获取对应父节点的下标
                                int p_j=parentIndex/width;///商表示行
                                int p_i=parentIndex%width;///余数表示列

                                ////将该点加入对应集水盆
                                blockData[j][i]=blockData[p_j][p_i];



                                addedPixes++;
                            }else if(num>=2){
                                ////该点标记为山脊
                                blockData[j][i]=RIDGE;

                                addedPixes++;
                            }
                            ////如果周围没有集水盆则不管

                        }


                    }
                }////end of  outer the double for-clause

                System.out.println(addedPixes+"::::"+altitude);
            }while(addedPixes>0);

        }/////end of the most outer for-clause

    }



    public Bitmap showWatershededImage(File file) throws IOException{


        Bitmap image=Bitmap.createBitmap(width, height,Bitmap.Config.RGB_565);

        for(int j=0;j<height;j++){
            for(int i=0;i<width;i++){
                ///将山脊像素点设置为白色，其它为黑色
                if(blockData[j][i]!=RIDGE){
                    int rgb=(255<<16)|(255<<8)|255;
                    image.setPixel(i, j, rgb);
                }else{
                    image.setPixel(i, j, 0);
                }
            }
        }

        //对图片进行放大处理
        Matrix matrix=new Matrix();
        matrix.postScale(2.0f,2.0f);
        Bitmap newBitmap=Bitmap.createBitmap(image,0,0,image.getWidth(),image.getHeight(),matrix,true);
        imageView.setImageBitmap(newBitmap);

        /*原逻辑为输出为文件，此处安卓版本修改为显示修改后的ImageView
        if(!file.exists()){
            file.createNewFile();
        }
        try {
            ImageIO.write(image, "jpg", file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */


        return image;
    }
	/*
	*//**
     * 分块区域数据重构
     *//*
	public void rebuildBlockData(){
		for(int j=0;j<height;j++){
			for(int i=0;i<width;i++){
				///将山脊区域标记为-1，非山脊区域标记为0
				if(blockData[j][i]==RIDGE){
					blockData[j][i]=-1;
				}else{
					blockData[j][i]=0;

				}
			}
		}
	}

	*//**
     * 区域合并
     *//*
	public void areaCombine(){

	}

	*/


    /**
     * 初始化函数，初始化集水盆部分比较难！！！
     * @param file
     */
    private void init(File file){
        try {
            try {
                sourceImg= BitmapFactory.decodeFile(file.getAbsolutePath());
            }catch (OutOfMemoryError e){
                //处理图片过大导致内存溢出的情况
                BitmapFactory.Options options=new BitmapFactory.Options();
                options.inSampleSize=2;
                sourceImg=BitmapFactory.decodeFile(file.getAbsolutePath(),options);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        double[][] arr=Image_Utility.gaussTran(Image_Utility.imageToDoubleArray(sourceImg), 5);

//		arr=ImageTransform.gaussTran(arr, 5);

        gradImage=Image_Utility.doubleArrayToGreyImage(arr);
//
        ////获得sobel梯度图像
        gradImage=Image_Utility.sobleTran(gradImage, THRESHOD);

        Image_Utility.imageToFile(gradImage, new File("/storage/emulated/0/data/gauss-grad.jpg"));


        width=gradImage.getWidth();
        height=gradImage.getHeight();
        blockData=new int[height][width];

        ////获取图像的最大梯度值，
        ///并产生初始化集水盆,也就是初始化bolckData这个多叉树
        maxGrad=0;
        for(int j=0;j<height;j++){
            for(int i=0;i<width;i++){
                ////此处的灰度值即为梯度值
                int grad=(gradImage.getPixel(i, j)>>16)&0xFF;

                if(grad>maxGrad){
                    maxGrad=grad;
                }


                /////这里是使用并查集思想实现区域标记和合并,很重要
                ////如果梯度值小于等等于阈值（在求sobel图像时已经统一赋值为零），标记之
                if(grad==0){
                    ////本应该检查八邻域的梯度值
                    ////由于采用逐行从左到右扫描，所以也只需判断上面三个、左一个共四个  点事否被标记
                    /////此处有两种情况，周围只有同一个被标记的区域，或者有两个被标记的区域：左、左上为一个区域，而右上点为一个区域，正上方肯定不被标记
                    int top=-1;
                    if(j>0){
                        top=(gradImage.getPixel(i,j-1)>>16)&0xFF;
                    }
                    int left=-1;
                    if(i>0){
                        left=(gradImage.getPixel(i-1,j)>>16)&0xFF;
                    }

                    int top_left=-1;
                    if(i>0&&j>0){
                        top_left=(gradImage.getPixel(i-1,j-1)>>16)&0xFF;
                    }
                    int top_right=-1;
                    if(i<width-1&&j>0){
                        top_right=(gradImage.getPixel(i+1,j-1)>>16)&0xFF;
                    }

                    /////此处的正左、左上、正上如果被标记，则肯定为统一区域，所以用同一个变量表示，右上用另一个变量表示
                    int left_top_area=-1;
                    int right_top_area=-1;
                    if(top==0){
                        left_top_area=blockData[j-1][i];
                    }
                    if(left==0){
                        left_top_area=blockData[j][i-1];
                    }
                    if(top_left==0){
                        left_top_area=blockData[j-1][i-1];
                    }

                    if(top_right==0){
                        right_top_area=blockData[j-1][i+1];
                    }




                    if(left_top_area==-1&&right_top_area==-1){
                        ////新区域
                        blockData[j][i]=j*width+i;
                        map.put(j*width+i, j*width+i);
                    }else if(left_top_area!=-1&&right_top_area==-1){
                        ///只有第一个区域被标记

                        int parentIndex=left_top_area;
                        ////获取对应父节点的下标
                        int p_j=parentIndex/width;///商表示行
                        int p_i=parentIndex%width;///余数表示列
                        ////指向父节点的父节点，这样做可以有效减小树的高度
                        blockData[j][i]=blockData[p_j][p_i];

                    }else if(left_top_area==-1&&right_top_area!=-1){
                        ///只有第二个区域被标记

                        int parentIndex=right_top_area;
                        ////获取对应父节点的下标
                        int p_j=parentIndex/width;///商表示行
                        int p_i=parentIndex%width;///余数表示列
                        ////指向父节点的父节点，这样做可以有效减小树的高度
                        blockData[j][i]=blockData[p_j][p_i];
                    }else if(left_top_area!=-1&&right_top_area!=-1){
                        ///该点周围有两个标记区域，判断是否为不同的区域而需要进行合并！


                        ////获取第一个区域对应根节点的下标
                        int first_parentIndex=left_top_area;

                        int first_j=first_parentIndex/width;///商表示行
                        int first_i=first_parentIndex%width;///余数表示列
                        while(blockData[first_j][first_i]!=first_parentIndex){
                            first_parentIndex=blockData[first_j][first_i];
                            ////获取对应父节点的下标
                            first_j=first_parentIndex/width;///商表示行
                            first_i=first_parentIndex%width;///余数表示列
                        }

                        ///找到左边区域的根节点即某节点指向自身
                        int second_parentIndex=right_top_area;
                        int second_j=second_parentIndex/width;///商表示行
                        int second_i=second_parentIndex%width;///余数表示列

                        while(blockData[second_j][second_i]!=second_parentIndex){
                            second_parentIndex=blockData[second_j][second_i];
                            second_j=second_parentIndex/width;///商表示行
                            second_i=second_parentIndex%width;///余数表示列
                        }


                        if(first_parentIndex!=second_parentIndex){
                            //////需要合并，第二个区域指向第一个区域
                            blockData[second_j][second_i]=first_parentIndex;
                            map.remove(second_parentIndex);
                        }

                        blockData[j][i]=first_parentIndex;

                    }



                }else{
                    ////表示未知区域
                    blockData[j][i]=-1;
                }



            }
        }///end of outer for-clause
    }




}
