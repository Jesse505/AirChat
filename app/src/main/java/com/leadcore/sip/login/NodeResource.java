package com.leadcore.sip.login;

public class NodeResource
{
  protected String bussinessType;
  protected String displayName;
  protected boolean hasCheckBox;
  protected int iconId;
  protected String index;
  protected boolean isGroup;
  protected String name;
  protected String number;
  protected String superIndex;
  protected String uri;
  protected String userType;
  
 
  public NodeResource()
  {
    
  }
  
  public NodeResource(String paramString1, String paramString2, String paramString3, String paramString4)
  {
    this.index = paramString1;
    this.superIndex = paramString1;
    this.name = paramString2;
    this.displayName = paramString2;
    this.uri = paramString3;
    this.number = paramString4;
    this.isGroup = false;
  }
  
  public NodeResource(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, int paramInt, boolean paramBoolean, String paramString6)
  {
    this.superIndex = paramString1;
    this.uri = paramString2;
    this.name = paramString3;
    this.number = paramString3;
    this.displayName = paramString4;
    this.bussinessType = paramString5;
    this.isGroup = paramBoolean;
    this.iconId = paramInt;
    this.userType = paramString6;
  }
  
  public NodeResource(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, boolean paramBoolean1, int paramInt, boolean paramBoolean2)
  {
    this.index = paramString1;
    this.superIndex = paramString2;
    this.name = paramString3;
    this.uri = paramString4;
    this.displayName = paramString5;
    this.number = paramString6;
    this.isGroup = paramBoolean1;
    this.iconId = paramInt;
    this.hasCheckBox = paramBoolean2;
  }
  
  public NodeResource(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, boolean paramBoolean1, int paramInt, boolean paramBoolean2, String paramString7)
  {
    this.index = paramString1;
    this.superIndex = paramString2;
    this.name = paramString3;
    this.uri = paramString4;
    this.displayName = paramString5;
    this.number = paramString6;
    this.isGroup = paramBoolean1;
    this.iconId = paramInt;
    this.hasCheckBox = paramBoolean2;
    this.userType = paramString7;
  }
  
  public NodeResource(String paramString1, String paramString2, String paramString3)
  {
    this.index = paramString1;
   
    this.displayName = paramString2;
    
    this.number = paramString3;
 
  }
  
 
  
  public String getBussinessType()
  {
    return this.bussinessType;
  }
  
  public String getDisplayName()
  {
    return this.displayName;
  }
  
  public int getIconId()
  {
    return this.iconId;
  }
  
  public String getIndex()
  {
    return this.index;
  }
  
  public boolean getIsGroup()
  {
    return this.isGroup;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public String getNumber()
  {
    return this.number;
  }
  
  public String getSuperIndex()
  {
    return this.superIndex;
  }
  
  public String getUri()
  {
    return this.uri;
  }
  
  public String getUserType()
  {
    return this.userType;
  }
  
  public boolean hasCheckBox()
  {
    return this.hasCheckBox;
  }
  
  public void setBussinessType(String paramString)
  {
    this.bussinessType = paramString;
  }
  
  public void setDisplayName(String paramString)
  {
    this.displayName = paramString;
  }
  
  public void setHasCheckBox(boolean paramBoolean)
  {
    this.hasCheckBox = paramBoolean;
  }
  
  public void setIconId(int paramInt)
  {
    this.iconId = paramInt;
  }
  
  public void setIndex(String paramString)
  {
    this.index = paramString;
  }
  
  public void setIsGroup(boolean paramBoolean)
  {
    this.isGroup = paramBoolean;
  }
  
  public void setName(String paramString)
  {
    this.name = paramString;
  }
  
  public void setNumber(String paramString)
  {
    this.number = paramString;
  }
  
  public void setSuperIndex(String paramString)
  {
    this.superIndex = paramString;
  }
  
  public void setUri(String paramString)
  {
    this.uri = paramString;
  }
  
  public void setUserType(String paramString)
  {
    this.userType = paramString;
  }
}


/* Location:           D:\tools\反编译工具\dex2jar和JD-GUI\classes_dex2jar.jar
 * Qualified Name:     com.sunkaisens.skdroid.component.NodeResource
 * JD-Core Version:    0.7.0.1
 */