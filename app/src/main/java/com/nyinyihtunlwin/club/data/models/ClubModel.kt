package com.nyinyihtunlwin.club.data.models

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.nyinyihtunlwin.club.data.vos.CompanyVo
import com.nyinyihtunlwin.club.data.vos.MemberVo
import com.nyinyihtunlwin.club.persistence.ClubDb
import com.nyinyihtunlwin.club.utils.AppConstants
import com.nyinyihtunlwin.club.utils.ConfigUtils
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class ClubModel : BaseModel() {

    companion object {
        private var INSTANCE: ClubModel? = null
        fun getInstance(): ClubModel {
            if (INSTANCE == null) {
                INSTANCE = ClubModel()
            }
            val i = INSTANCE
            return i!!
        }
    }

    fun initDatabase(context: Context) {
        mDatabase = ClubDb.getDatabase(context)
    }

    fun getClubData(
        responseLd: MutableLiveData<List<CompanyVo>>,
        errorLd: MutableLiveData<String>
    ) {
        mApi.getClubData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                object : Observer<List<CompanyVo>> {
                    override fun onComplete() {
                    }

                    override fun onSubscribe(p0: Disposable) {
                    }

                    override fun onNext(response: List<CompanyVo>) {
                        if (response.isNotEmpty()) {
                            saveToDb(response)
                            responseLd.value = getCompanies()
                        } else {
                            errorLd.value = "No Data"
                        }
                    }

                    override fun onError(p0: Throwable) {
                        errorLd.value = p0.localizedMessage
                    }
                })
    }

    private fun saveToDb(response: List<CompanyVo>) {
        mDatabase.companyDao().insertCompanies(response)
        for (res in response) {
            for (mem in res.members) {
                mem.companyId = res.companyId
            }
            mDatabase.memberDao().insertMembers(res.members)
        }
    }

    fun getCompanies(): List<CompanyVo> {
        val loadComSortOrder = ConfigUtils.getInstance().loadComSortOrder()
        return when (loadComSortOrder) {
            AppConstants.COMPANY_ORDER_DEFAULT -> {
                mDatabase.companyDao().getCompanies()
            }
            AppConstants.COMPANY_ORDER_ASC -> {
                mDatabase.companyDao().getCompaniesAscOrder()
            }
            AppConstants.COMPANY_ORDER_DESC -> {
                mDatabase.companyDao().getCompaniesDescOrder()
            }
            else -> {
                mDatabase.companyDao().getCompanies()
            }
        }
    }

    fun getMembersByCompany(companyId: String): List<MemberVo>? {
        return mDatabase.memberDao().getMembersByCompany(companyId)
    }
}