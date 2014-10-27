package com.example.mykola.mydicom;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class DCMParser{} /*implements DicomInputHandler {

    private static final int DEF_MAX_WIDTH = 128;
    private static final int DEF_MAX_VAL_LEN = 128;

    private StringBuffer line = new StringBuffer();
    List<String> dcmInfo = new LinkedList<String>();
    private char[] cbuf = new char[DEF_MAX_VAL_LEN];
    private int maxWidth = DEF_MAX_WIDTH;
    private int maxValLen = DEF_MAX_VAL_LEN;

    public final void setMaxValLen(int maxValLen) {
        this.maxValLen = maxValLen;
    }

    public final void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public List<String> dump(String fileName) throws IOException {

        try {
            File ifile = new File(fileName);
            DicomInputStream dis = new DicomInputStream(ifile);

            try {
                dis.setHandler(this);
                dis.readDicomObject(new BasicDicomObject(), -1);
            } finally {
                dis.close();
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        return dcmInfo;
    }

    public boolean readValue(DicomInputStream in) throws IOException {
        switch (in.tag()) {
            case Tag.Item:
                if (in.sq().vr() != VR.SQ && in.valueLength() != -1) {
                    outFragment(in);
                } else {
                    outItem(in);
                }
                break;
            case Tag.ItemDelimitationItem:
            case Tag.SequenceDelimitationItem:
                if (in.level() > 0)
                    outItem(in);
                break;
            default:
                outElement(in);
        }
        return true;
    }

    private void outElement(DicomInputStream in) throws IOException {
        outTag(in);
        outVR(in);
        outLen(in);

        if (hasItems(in)) {
            outName(in);
            outLine();
            readItems(in);
        } else {
            outName(in);
            outValue(in);
            outLine();
        }
    }

    private void outValue(DicomInputStream in) throws IOException {
        int tag = in.tag();
        VR vr = in.vr();
        byte[] val = in.readBytes(in.valueLength());
        DicomObject dcmobj = in.getDicomObject();
        boolean bigEndian = in.getTransferSyntax().bigEndian();
        line.append(" [");
        vr.promptValue(val, bigEndian, dcmobj.getSpecificCharacterSet(), cbuf,
                maxValLen, line);
        line.append("]");
        if (tag == Tag.SpecificCharacterSet || tag == Tag.TransferSyntaxUID
                || TagUtils.isPrivateCreatorDataElement(tag)) {
            dcmobj.putBytes(tag, vr, val, bigEndian);
        }
        if (tag == 0x00020000) {
            in.setEndOfFileMetaInfoPosition(in.getStreamPosition()
                    + vr.toInt(val, bigEndian));
        }
    }

    private boolean hasItems(DicomInputStream in) {
        return in.valueLength() == -1 || in.vr() == VR.SQ;
    }

    private void readItems(DicomInputStream in) throws IOException {
        in.readValue(in);
        in.getDicomObject().remove(in.tag());
    }

    private void outItem(DicomInputStream in) throws IOException {
        outTag(in);
        outLen(in);
        outName(in);
        in.readValue(in);
    }

    private void outFragment(DicomInputStream in) throws IOException {
        outTag(in);
        outLen(in);
        outName(in);
        outLine();

        in.readValue(in);
        DicomElement sq = in.sq();
        byte[] data = sq.removeFragment(0);
        boolean bigEndian = in.getTransferSyntax().bigEndian();
        line.append(" [");
        sq.vr().promptValue(data, bigEndian, null, cbuf, maxValLen, line);
        line.append("]");


    }

    private void outTag(DicomInputStream in) {
        line.setLength(0);
        TagUtils.toStringBuffer(in.tag(), line);
    }

    private void outVR(DicomInputStream in) {
        line.append(" ").append(in.vr());
    }

    private void outLen(DicomInputStream in) {
        line.append(" #").append(in.valueLength());
    }

    private void outName(DicomInputStream in) {
        line.append(" ").append(in.getDicomObject().nameOf(in.tag()));
    }

    private void outLine() {
        if (line.length() > maxWidth)
            line.setLength(maxWidth);
        dcmInfo.add(line.toString());
    }

}*/
