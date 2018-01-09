package com.example.nicoladalmaso.gruppo1;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import export.ExportedFile;

public class EmailBuilder{

    public static Email createEmail(){
        return new Email();
    }


}
class Email{
    private String receiver, subject;
    private List<Uri> attachments = new ArrayList<>();

    public Email to(String receiver){
        this.receiver = receiver;
        return this;
    }

    public Email attachFile(File file){
        attachments.add(Uri.fromFile(file));
        return this;
    }

    public Email attachFiles(List<ExportedFile> files){
        for(ExportedFile exfile : files){
            attachFile(exfile.file);
        }
        return this;
    }

    public Email subject(String subject){
        this.subject = subject;
        return this;
    }

    public void sendEmail(Context context){
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("vnd.android.cursor.dir/email");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, receiver);
// the attachments
        for(Uri attachment : attachments){
            emailIntent.putExtra(Intent.EXTRA_STREAM, attachment);
        }
// the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        context.startActivity(Intent.createChooser(emailIntent , "Send email..."));
    }

}