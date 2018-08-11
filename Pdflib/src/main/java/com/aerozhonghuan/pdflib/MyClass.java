package com.aerozhonghuan.pdflib;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class MyClass {

    public static void main(String[] args) throws IOException {

        printf("ready");

        PDDocument document = null;
        PDDocument docW = null;
        try {
            document = PDDocument.load(new File("/Users/zhangyunfei/Downloads/120张婴儿黑白视觉卡.pdf"));
            int numberOfPages_origin = document.getNumberOfPages();
            printf("image total number is: " + numberOfPages_origin);
            printf("");
            PDFRenderer renderer = new PDFRenderer(document);

            int imgOutWidth = 320;
            int imgOutHeight = 320;

            for (int i = 1; i < numberOfPages_origin; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 96); // Windows native DPI
                BufferedImage srcImage = resize(image, imgOutWidth, imgOutHeight);//产生缩略图
                ImageIO.write(srcImage, "PNG", new File("/Users/zhangyunfei/Downloads/ccc/img" + (i-1) + ".png"));
            }
            document.close();

            int imgNum_PerPage = 4;//每页4个
            int max_age = numberOfPages_origin / imgNum_PerPage + 1;
            docW = new PDDocument();
            for (int i = 0; i < max_age; i++) {
                printf("*********** new page number is " + i);
                printf("");
                PDPage my_page = new PDPage();

                int x = 5;
                int y = 80;
                PDPageContentStream contentStream = new PDPageContentStream(docW, my_page);
                for (int j = 0; j < imgNum_PerPage; j++) {
                    int pageN = imgNum_PerPage * i + j;
                    String path = String.format("/Users/zhangyunfei/Downloads/ccc/img%s.png", pageN);
                    if (!new File(path).exists()) break;

                    PDImageXObject pdImage = PDImageXObject.createFromFile(path, docW);
                    contentStream.drawImage(pdImage, x, 570 - y);//矫正，翻转Y坐标系
                    printf("write image: " + path);
                    x += imgOutWidth;
                    if (x > 2 * imgOutWidth) {
                        x = 5;
                        y += imgOutWidth + 10;
                    }
                    printf(String.format("curent:(%s,%s) ", x, y));
                }
                contentStream.close();
                docW.addPage(my_page);
            }
            docW.save("/Users/zhangyunfei/Downloads/ccc/_1.pdf");
            docW.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (document != null)
                document.close();

            if (docW != null)
                docW.close();
        }
    }


    private static BufferedImage resize(BufferedImage source, int targetW, int targetH) {
        int type = source.getType();
        BufferedImage target = null;
        double sx = (double) targetW / source.getWidth();
        double sy = (double) targetH / source.getHeight();
        if (sx > sy) {
            sx = sy;
            targetW = (int) (sx * source.getWidth());
        } else {
            sy = sx;
            targetH = (int) (sy * source.getHeight());
        }
        if (type == BufferedImage.TYPE_CUSTOM) {
            ColorModel cm = source.getColorModel();
            WritableRaster raster = cm.createCompatibleWritableRaster(targetW, targetH);
            boolean alphaPremultiplied = cm.isAlphaPremultiplied();
            target = new BufferedImage(cm, raster, alphaPremultiplied, null);
        } else {
            target = new BufferedImage(targetW, targetH, type);
        }
        Graphics2D g = target.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawRenderedImage(source, AffineTransform.getScaleInstance(sx, sy));
        g.dispose();
        return target;
    }

    private static void printf(String str) {
        System.out.println("" + str);
    }


//    public static class MyPoint {
//        public MyPoint(int x, int y) {
//            this.x = x;
//            this.y = 570 - y;//矫正，翻转Y坐标系
//        }
//
//        public int x;
//        public int y;
//    }
}
