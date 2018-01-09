package com.example.nicoladalmaso.gruppo1;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import export.ExportedFile;

/**
 * @author Federico Taschin
 * This class creates an istance of Email
 */
public class EmailBuilder{
    public static Email createEmail(){
        return new Email();
    }
}

/**
 * @author Federico Taschin
 * This class builds an e-mail by setting the requested field and invokes the email client to send it
 */
class Email{
    private String receiver, subject;
    private List<Uri> attachments = new ArrayList<>();

    public Email to(String receiver){
        this.receiver = receiver;
        return this;
    }

    /**
     * @author Federico Taschin
     * adds the file to add to the attachments
     * @param file the file to attach to the mail
     * @return the modified instance of Email
     */
    public Email attachFile(File file){
        attachments.add(Uri.fromFile(file));
        return this;
    }

    /**
     * @author Federico Taschin
     * @param files files to add to the attachments
     * @return the modified instance of Email
     */
    public Email attachFiles(List<ExportedFile> files){
        for(ExportedFile exfile : files){
            attachFile(exfile.file);
        }
        return this;
    }

    /**
     * @author Federico Taschin
     * @param subject the subject of the mail
     * @return the modified instance of Email
     */
    public Email subject(String subject){
        this.subject = subject;
        return this;
    }

    /**
     * @author Federico Taschin
     * @param context the context of the activity sending the mail
     */
    public void sendEmail(Context context){
        String type ="vnd.android.cursor.dir/email";
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType(type);
        //the receiver
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