package com.example.nicoladalmaso.gruppo1;

import android.graphics.Bitmap;

import org.junit.Test;
import org.mockito.Mock;

import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


/**
 * Created by Cristian on 01/12/2017.
 */
public class BillActivityTest extends BillActivity {
    BillActivity test=new BillActivity();
    @Test
   public void createImageFileTest()
    {   File testDirectory=new File("bagigio");
        File exitFile=new File("");
        try{
        exitFile=createImageFile(testDirectory);
        }
        catch(java.io.IOException e){  }
        String directory=exitFile.getAbsolutePath();
        assertEquals("bagigio.jpg", directory);

    }

}