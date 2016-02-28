- IngressRobot 
	- Version: 
		- 1.0.0.0
		  . Auto hack on specified portal.
		- 1.0.0.2
		  . Auto drop & Auto acquire (1080x720, ex: HTC mini)
		- 1.0.0.3
		  . Auto drop key & Auto acquire key (1280x720, ex HTC mini)
		  . Auto drop key & Auto acquire key (1920x1080, ex HTC butteryfly)
		  . Auto drop & Auto acquire (1920x1080, ex HTC butteryfly)
		- 1.0.0.4
		  . Fine tune the performance of Auto drop & auto acquire.
	- Feature Behavior
		- Auto Hack: click the position of portal -> click hack button -> click ok button.
		- Auto Drop: click the position of OPT button -> click middle position -> click the drop button.
		- Auto Drop key: click the position of OPT button -> click middle position -> click the drop button.
		- Auro Acquire: click the position of res -> click acquire
	- Tool:
		. IngressRobotHost   -> You can take device erverwhere. It must have root access right.(i.e. su)
		. IngressRobotScript -> Actions that integrate with Uiautomator.
	- How to install
		Steps
			1. adb push bin\IngressRobotScript.jar /data/local/tmp
			2. adb install IngressRobotHost.apk
	- Limitation
		. Sense 5+ (Only htc device)
		. Android SDK: > 17 (Uiautomator supported)
		. The language: English
		. Resolution: 1280x720, 1920x1080
	- Data Path
		. Config file path: /sdcard/ingressrobot/reobot.config
 
	- Run with shell command (It not need have su access right.)
		. You can only push IngressRobotScript.jar to run script.
		. It not need have su access right.
		. It must have link with PC.

		Steps		
			1. Create one file name as "reobot.config" to set portal, hack number parametere.
				Ex: 
				portal_1=582,1100
				portal_2=112,122
				portal_4=233,243
				portal_3=342,343
				hack_number=30
			2. adb push  reobot.config /sdcard/ingressrobot/reobot.config
			3. adb shell uiautomator runtest IngressRobotScript.jar -c com.autoingress.run.AutoHack
