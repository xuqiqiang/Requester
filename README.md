[![](https://jitpack.io/v/xuqiqiang/Requester.svg)](https://jitpack.io/#xuqiqiang/Requester)

# Requester
一行代码请求本地资源或权限等

## Gradle dependency

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
	implementation 'com.github.xuqiqiang:Requester:1.0.0'
}
```

## Usage

**请求权限**

- 支持请求多个权限
- 支持传入任意类型的Context (Activity/Service/Application)
- 如果用户拒绝授权并勾选了「不再询问」，将引导用户跳转至权限设置页面，回到应用后会回调最终结果
- 支持请求特殊权限 (允许通知/安装未知来源应用/允许修改系统设置/允许设置悬浮窗)
- 不存在兼容性问题

请求单个权限

```java
PermissionRequester.request(context, success -> {}, Manifest.permission.CAMERA);
```

请求特殊权限

```java
PermissionRequester.requestSpecialPermission(context, DISPLAY_NOTIFICATION, success -> {});
```

**请求本地资源**

跳转至资源管理器，选择一张图片

```java
PickerRequester.pickImage(context, filePath -> {});
```

跳转至资源管理器，选择一个视频

```java
PickerRequester.pickVideo(context, filePath -> {});
```

跳转至通讯录，选择一个联系人

```java
PickerRequester.pickContact(context, uri -> {});
```

**请求拍照**

跳转至相机，拍张照片

```java
CaptureRequester.capImage(context, filePath -> {});
```

跳转至相机，录制一段视频

```java
CaptureRequester.capVideo(context, filePath -> {});
```

**请求设备解锁**

```java
KeyguardRequester.requestAuthentication(context, success -> {});
```

**请求桌面快捷方式**

- Android O以下通过广播的方式创建桌面快捷方式
- Android O及以上调用相关API创建快捷方式
- 如果没有权限，将引导用户跳转至权限设置页面，回到应用后会立即创建

```java
ShortcutRequester.requestPinShortcut(context, id, name, TargetActivity.class, icon, data, success -> {});
```

**请求Activity回调事件**

```java
ActivityRequester.startActivityForResult(context, intent, (resultCode, data) -> {});
```

```java
ActivityRequester.postOnResume(activity, () -> {});
```

```java
ActivityRequester.postOnDestroyed(activity, () -> {});
```

**请求下载文件**

```java
DownloadRequester.download(context, DOWNLOAD_URL, filePath -> {});
```

**请求屏幕录制**

- 不支持Android L以下的系统版本

```java
new ScreenRecorderRequester(context).startCapturing(new ScreenRecorderListener());
```

## Apache License
       Apache License
    
       Copyright [2020] [xuqiqiang]
    
       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at
    
           http://www.apache.org/licenses/LICENSE-2.0
    
       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.

