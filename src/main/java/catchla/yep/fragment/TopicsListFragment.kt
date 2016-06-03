package catchla.yep.fragment

import android.accounts.Account
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.DialogFragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import catchla.yep.Constants
import catchla.yep.R
import catchla.yep.activity.*
import catchla.yep.adapter.TopicsAdapter
import catchla.yep.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition
import catchla.yep.fragment.iface.IActionButtonSupportFragment
import catchla.yep.loader.DiscoverTopicsLoader
import catchla.yep.model.Attachment
import catchla.yep.model.LocationAttachment
import catchla.yep.model.Paging
import catchla.yep.model.Topic
import catchla.yep.view.holder.TopicViewHolder

/**
 * Created by mariotaku on 15/10/12.
 */
class TopicsListFragment : AbsContentListRecyclerViewFragment<TopicsAdapter>(), LoaderManager.LoaderCallbacks<List<Topic>>, TopicsAdapter.TopicClickListener, IActionButtonSupportFragment {

    @Topic.SortOrder
    private var mSortBy: String? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //noinspection WrongConstant
        mSortBy = mPreferences.getString(Constants.KEY_TOPICS_SORT_ORDER, Topic.SortOrder.DEFAULT)
        setHasOptionsMenu(true)
        val fragmentArgs = arguments
        val loaderArgs = Bundle()
        if (fragmentArgs != null) {
            loaderArgs.putBoolean(Constants.EXTRA_READ_CACHE, !fragmentArgs.containsKey(Constants.EXTRA_LEARNING) && !fragmentArgs.containsKey(Constants.EXTRA_MASTER))
            if (fragmentArgs.containsKey(Constants.EXTRA_USER_ID)) {
                loaderArgs.putString(Constants.EXTRA_USER_ID, fragmentArgs.getString(Constants.EXTRA_USER_ID))
            }
        } else {
            loaderArgs.putBoolean(Constants.EXTRA_READ_CACHE, true)
        }
        loaderManager.initLoader(0, loaderArgs, this)
        adapter.clickListener = this
        showProgress()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<Topic>> {
        val cachingEnabled = isCachingEnabled
        val readCache = args!!.getBoolean(Constants.EXTRA_READ_CACHE) && cachingEnabled
        val readOld = args.getBoolean(Constants.EXTRA_READ_OLD, readCache) && cachingEnabled
        val maxId = args.getString(Constants.EXTRA_MAX_ID)
        val paging = Paging()
        if (maxId != null) {
            paging.maxId(maxId)
        }
        val oldData: List<Topic>?
        if (readOld) {
            oldData = adapter.topics
        } else {
            oldData = null
        }
        return DiscoverTopicsLoader(activity, account, arguments.getString(Constants.EXTRA_USER_ID),
                paging, sortOrder, readCache, cachingEnabled, oldData)
    }

    private val sortOrder: String
        @Topic.SortOrder
        get() {
            if (hasUserId()) return Topic.SortOrder.TIME
            return if (mSortBy != null) mSortBy!! else Topic.SortOrder.TIME
        }

    private fun hasUserId(): Boolean {
        val fragmentArgs = arguments
        return fragmentArgs != null && fragmentArgs.containsKey(Constants.EXTRA_USER_ID)
    }

    override fun onLoadFinished(loader: Loader<List<Topic>>, data: List<Topic>?) {
        val adapter = adapter
        adapter.topics = data
        adapter.loadMoreSupportedPosition = if (data != null && !data.isEmpty()) IndicatorPosition.END else IndicatorPosition.NONE
        showContent()
        isRefreshing = false
        setRefreshEnabled(true)
        setLoadMoreIndicatorPosition(IndicatorPosition.NONE)
    }

    override fun onLoaderReset(loader: Loader<List<Topic>>) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_NEW_LOCATION_TOPIC -> {
                if (resultCode == Activity.RESULT_OK) {
                    val name = data!!.getStringExtra(Constants.EXTRA_NAME)
                    val location = data.getParcelableExtra<Location>(Constants.EXTRA_LOCATION)
                    val intent = Intent(context, NewTopicActivity::class.java)
                    val attachment = LocationAttachment()
                    attachment.place = name
                    attachment.latitude = location.latitude
                    attachment.longitude = location.longitude
                    intent.putExtra(Constants.EXTRA_NEW_TOPIC_TYPE, NewTopicActivity.TYPE_LOCATION)
                    intent.putExtra(Constants.EXTRA_ATTACHMENT, attachment)
                    intent.putExtra(Constants.EXTRA_ACCOUNT, account)
                    startActivity(intent)
                    return
                }
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateAdapter(context: Context): TopicsAdapter {
        return TopicsAdapter(context)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_fragment_chats_list, menu)
    }

    override fun onBaseViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onBaseViewCreated(view, savedInstanceState)
    }

    override fun onRefresh() {
        val loaderArgs = Bundle()
        loaderArgs.putBoolean(Constants.EXTRA_READ_CACHE, false)
        loaderManager.restartLoader(0, loaderArgs, this)
    }

    override fun isRefreshing(): Boolean {
        return loaderManager.hasRunningLoaders()
    }

    val isCachingEnabled: Boolean
        get() = arguments.getBoolean(Constants.EXTRA_CACHING_ENABLED)

    override fun onItemClick(position: Int, holder: RecyclerView.ViewHolder) {
        val topic = adapter.getTopic(position)
        val intent = Intent(activity, TopicChatActivity::class.java)
        intent.putExtra(Constants.EXTRA_ACCOUNT, account)
        intent.putExtra(Constants.EXTRA_TOPIC, topic)
        startActivity(intent)
    }

    private val account: Account
        get() = arguments.getParcelable<Account>(Constants.EXTRA_ACCOUNT)

    override fun getActionIcon(): Int {
        return R.drawable.ic_action_edit
    }

    override fun onActionPerformed() {
        val args = Bundle()
        args.putParcelable(Constants.EXTRA_ACCOUNT, account)
        val df = NewTopicTypeDialogFragment()
        df.arguments = args
        df.show(childFragmentManager, "new_topic_type")
    }

    override fun getActionMenuFragment(): Class<out FloatingActionMenuFragment>? {
        return TopicsMenuFragment::class.java
    }

    override fun onLoadMoreContents(@IndicatorPosition position: Int) {
        // Only supports load from end, skip START flag
        if (position and IndicatorPosition.START != 0) return
        super.onLoadMoreContents(position)
        val loaderArgs = Bundle()
        loaderArgs.putBoolean(Constants.EXTRA_READ_CACHE, false)
        loaderArgs.putBoolean(Constants.EXTRA_READ_OLD, true)
        val adapter = adapter
        val topicsCount = adapter.topicsCount
        if (topicsCount > 0) {
            loaderArgs.putString(Constants.EXTRA_MAX_ID, adapter.getTopic(topicsCount - 1).id)
        }
        loaderManager.restartLoader(0, loaderArgs, this)
    }

    override fun onSkillClick(position: Int, holder: TopicViewHolder) {
        val intent = Intent(context, SkillUpdatesActivity::class.java)
        intent.putExtra(Constants.EXTRA_ACCOUNT, account)
        intent.putExtra(Constants.EXTRA_SKILL, adapter.getTopic(position).skill)
        startActivity(intent)
    }

    override fun onUserClick(position: Int, holder: TopicViewHolder) {
        val intent = Intent(context, UserActivity::class.java)
        intent.putExtra(Constants.EXTRA_ACCOUNT, account)
        intent.putExtra(Constants.EXTRA_USER, adapter.getTopic(position).user)
        startActivity(intent)
    }

    override fun onMediaClick(attachments: Array<Attachment>, attachment: Attachment, clickedView: View) {
        val intent = Intent(context, MediaViewerActivity::class.java)
        intent.putExtra(Constants.EXTRA_MEDIA, attachments)
        intent.putExtra(Constants.EXTRA_CURRENT_MEDIA, attachment)
        val location = IntArray(2)
        clickedView.getLocationOnScreen(location)
        intent.sourceBounds = Rect(location[0], location[1], location[0] + clickedView.width,
                location[1] + clickedView.height)
        val options = ActivityOptionsCompat.makeScaleUpAnimation(clickedView, 0, 0,
                clickedView.width, clickedView.height).toBundle()
        ActivityCompat.startActivity(activity, intent, options)
    }

    fun reloadWithSortOrder(@Topic.SortOrder sortBy: String) {
        if (TextUtils.equals(sortOrder, sortBy) || hasUserId()) return
        mSortBy = sortBy
        mPreferences.edit().putString(Constants.KEY_TOPICS_SORT_ORDER, sortBy).apply()
        val loaderArgs = Bundle()
        loaderArgs.putBoolean(Constants.EXTRA_READ_CACHE, false)
        loaderArgs.putBoolean(Constants.EXTRA_READ_OLD, false)
        loaderManager.restartLoader(0, loaderArgs, this)
        showProgress()
    }

    class NewTopicTypeDialogFragment : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context)
            val resources = resources
            val entries = resources.getStringArray(R.array.new_topic_type_entries)
            val values = resources.getStringArray(R.array.new_topic_type_values)
            builder.setItems(entries) { dialog, which ->
                when (values[which]) {
                    "photos_text" -> {
                        val intent = Intent(context, NewTopicActivity::class.java)
                        intent.putExtra(Constants.EXTRA_ACCOUNT, account)
                        startActivity(intent)
                    }
                    "audio" -> {
                    }
                    "location" -> {
                        val parent = parentFragment
                        val intent = Intent(context, LocationPickerActivity::class.java)
                        intent.putExtra(Constants.EXTRA_ACCOUNT, account)
                        parent.startActivityForResult(intent, REQUEST_NEW_LOCATION_TOPIC)
                    }
                }
            }
            return builder.create()
        }

        private val account: Account
            get() = arguments.getParcelable<Account>(Constants.EXTRA_ACCOUNT)
    }

    companion object {

        private val REQUEST_NEW_LOCATION_TOPIC = 102
    }
}
