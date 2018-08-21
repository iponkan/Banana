AFAIK: as far as i know

EGL10和EGL14的区别
现在GLSurfaceView用的是EGL10，EGL14在Grafika有使用的范例，这里我认为egl10已经能满足我们的需求。

They are just different versions of EGL, which is the OpenGL window system interface used on Android.

EGL10 corresponds to EGL 1.0, which is the spec released in 2003.
EGL14 corresponds to EGL 1.4, which is the spec released in 2014.
This means that EGL14 is much more recent. The latest spec is EGL 1.5, released in 2015.

The unfortunate aspect is that the Android Java bindings for these two versions are quite different.
While functionally EGL 1.4 is a superset of EGL 1.0 (at least AFAIK, I didn't compare the specs systematically),
EGL14 in Android is not an extended version of the EGL10 API. So you can't just mix and match functionality
between the two. You pretty much have to pick one, and stick with it.

Needless to say based on 11 years difference in release time, EGL 1.4 is greatly superior to EGL 1.0.

Now you might wonder why GLSurfaceView uses EGL10 references in its interface. I don't know for sure,
but I strongly suspect that it's for backwards compatibility. EGL14 was only added in API level 17,
while GLSurfaceView has been there since API level 3. To avoid breaking old apps, they would almost have to
introduce a distinct version of GLSurfaceView that ties in with EGL14.

If you want to use GLSurfaceView and EGL14 together, you have to jump through some hoops.
For example, if you have a EGLConfig object from the EGL10 interface, and want the corresponding EGLConfig object
for use with the EGL14 interface, the only way I found was to extract the config id from the original config,
using the EGL10 version of eglGetConfigAttrib(), and then query for the EGL14 config using the EGL14 version
of eglChooseConfig().

What adds to the "fun" when you start mixing the two versions is that they mostly use the same method names.
This means that you have to use qualified names in source files where you deal with both versions.