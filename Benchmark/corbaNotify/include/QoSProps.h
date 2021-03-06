#ifndef QOSPROPS_H
#define QOSPROPS_H
/*******************************************************************************
* ALMA - Atacama Large Millimiter Array
* Copyright (c) European Southern Observatory, 2014 
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
* 
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
*
*
* who       when      what
* --------  --------  ----------------------------------------------
* almadev  2014-08-28  created
*/

/************************************************************************
 *
 *----------------------------------------------------------------------
 */

#ifndef __cplusplus
#error This is a C++ include file and cannot be used from plain C
#endif


#include <orbsvcs/CosNotificationC.h>
#include <string>
#include <vector>



class QoSProps {
    public:
        QoSProps(const std::string &config);
        virtual ~QoSProps();    

        const CosNotification::QoSProperties& getEventProps() const;
        const CosNotification::QoSProperties& getProxyProps() const;
        const CosNotification::QoSProperties& getAdminProps() const;
        const CosNotification::QoSProperties& getChannelQoSProps() const;
        const CosNotification::AdminProperties& getChannelAdminProps() const;

    protected:
        void parseProperties(const std::string &config);
        void parseItems(const std::string &str,char sep,std::vector<std::string> &items);
        void getProperties(const std::vector<std::string> &propsList,char sep,CosNotification::PropertySeq &props);
        bool getBooleanValue(const std::string &value);

        CosNotification::QoSProperties eventProps;
        CosNotification::QoSProperties proxyProps;
        CosNotification::QoSProperties adminProps;
        CosNotification::QoSProperties channelQoSProps;
        CosNotification::AdminProperties channelAdminProps;


};

#endif
