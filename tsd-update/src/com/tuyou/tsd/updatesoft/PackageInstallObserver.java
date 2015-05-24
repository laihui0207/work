package com.tuyou.tsd.updatesoft;



import android.content.pm.IPackageInstallObserver;
import android.os.RemoteException;
import android.util.Log;

public class PackageInstallObserver extends IPackageInstallObserver.Stub
{
  @Override
  public void packageInstalled(String packageName, int returnCode) throws RemoteException 
  {
    Log.v("Update","PackageInstallObserver.packageInstalled() packageName:" + packageName
        + "  returnCode:" + returnCode);
  }
}
